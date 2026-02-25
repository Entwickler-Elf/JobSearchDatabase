package com.stromsland.jobsearchdatabase.service;

import com.stromsland.jobsearchdatabase.model.JobListing;
import com.stromsland.jobsearchdatabase.model.JobListingsEntity;
import com.stromsland.jobsearchdatabase.model.ScanEntity;
import com.stromsland.jobsearchdatabase.repository.JobListingsRepository;
import com.stromsland.jobsearchdatabase.repository.ScanRepository;
import org.slf4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JobSearchService {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JobSearchService.class);

    public record SearchRunSummary(
            String query,
            int newJobsCount,
            long totalJobsCount
    ) { }

    private final ChatClient chatClient;
    private final RestTemplate restTemplate;
    private final BeanOutputConverter<List<JobListing>> outputConverter;
    private final JobMapper jobMapper;

    private final JobListingsRepository jobListingsRepository;
    private final ScanRepository scanRepository;

    public JobSearchService(
            ChatClient.Builder chatClientBuilder,
            ToolCallbackProvider mcpTools,
            JobListingsRepository jobListingsRepository,
            ScanRepository scanRepository,
            JobMapper jobMapper
    ) {
        this.jobMapper = jobMapper;
        this.jobListingsRepository = jobListingsRepository;
        this.scanRepository = scanRepository;

        this.restTemplate = new RestTemplate();
        this.outputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<>() {});

        String systemPrompt = """
                You are a job search assistant integrated with external tools.
                
                Your job:
                - Call the Dice MCP tool `search_jobs` to retrieve job listings.
                - Return results as JSON that can be parsed into `List<JobListing>`.
                
                CRITICAL TOOL-CALL RULES (MUST FOLLOW EXACTLY)
                1) NEVER send null / None to any tool parameter.
                   - If you don't have a value, OMIT the parameter entirely OR use an explicit safe default as defined below.
                   - Do not send keys with null values.
                
                2) `search_jobs` parameters MUST be valid types:
                   - radius: number (float/int) -> ALWAYS provide a number (use default below if user didn’t specify)
                   - radius_unit: string -> ALWAYS provide a string (use default below if user didn’t specify)
                   - employer_types: list -> ALWAYS provide a list (empty list is OK)
                   - willing_to_sponsor: boolean -> ALWAYS provide true/false
                   - easy_apply: boolean -> ALWAYS provide true/false
                   - fields: list of strings -> ALWAYS provide a non-empty list
                
                3) FIELD SELECTION RULE:
                   - When calling `search_jobs`, DO NOT request `jobLocation` in the `fields` list.
                   - The allowed location field is exactly `jobLocation.displayName`.
                
                SAFE DEFAULTS (use these when the user does not specify)
                - radius: 120
                - radius_unit: "miles"
                - employer_types: []
                - willing_to_sponsor: false
                - easy_apply: false
                
                WORKFLOW
                A) Call `search_jobs` with:
                   - query: the user's query text (string)
                   - radius, radius_unit, employer_types, willing_to_sponsor, easy_apply, fields (all present, using defaults if needed)
                   - fields MUST include at least:
                     - "id"
                     - "title"
                     - "summary"
                     - "companyName"
                     - "jobLocation.displayName"
                     - "detailsPageUrl"
                     - "companyPageUrl"
                     - "salary"
                     - "employmentType"
                     - "workplaceTypes"
                     - "postedDate"
                     - "easyApply"
                
                
                OUTPUT REQUIREMENTS (what you return to the application)
                - Return ONLY valid JSON (no markdown, no prose).
                - The output must be a JSON array of objects matching `JobListing`.
                - Mapping rule:
                  - Map Dice "jobLocation.displayName" -> output field "jobLocation"
                - No nulls in output. Use empty strings "" for unknown text fields and false for booleans.
                
                QUALITY FILTERS (apply if user didn’t ask otherwise)
                - Prefer jobs posted within the last 14 days when `postedDate` allows it.
                - Prefer roles matching the query keywords.
                
                Remember:
                - Do not include any tool parameter with null/None.
                - Always include `fields` as a real list, never null.
                """;
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultToolCallbacks(mcpTools.getToolCallbacks())
                .defaultTools(this)
                .build();
    }

    public SearchRunSummary runSearchJobs(String query) {
        logger.info("Running search for query: {}", query);

        StopWatch stopWatch = new StopWatch("job-search");
        stopWatch.start("chat");

        List<JobListing> listings = chatClient.prompt()
                .user(query)
                .call()
                .entity(outputConverter);

        stopWatch.stop();
        logger.info("chat response {}", stopWatch.prettyPrint());

        stopWatch.start("save in database");

        List<JobListing> safeListings = (listings == null) ? Collections.emptyList() : listings;

        ScanEntity scan = buildScan(safeListings.size());
        ScanEntity savedScan = scanRepository.save(scan);

        List<JobListingsEntity> entitiesToInsert = safeListings.stream()
                // NOTE: existsById(listing.id()) is usually the wrong dedupe key for generated IDs,
                // but leaving your current rule intact for now.
                .filter(listing -> {
                    boolean exists = jobListingsRepository.existsById(listing.id());
                    if (exists) {
                        logger.info("Skipping listing already in DB (external id={})", listing.id());
                    }
                    return !exists;
                })
                .peek(listing -> logger.info("Will insert listing with external id={}", listing.id()))
                .map(listing -> toJobEntity(listing, savedScan))
                .toList();

        if (!entitiesToInsert.isEmpty()) {
            jobListingsRepository.saveAll(entitiesToInsert);
        }

        stopWatch.stop();
        logger.info("finished saving to the database {}", stopWatch.prettyPrint());

        long totalJobsCount = jobListingsRepository.count();
        return new SearchRunSummary(query, entitiesToInsert.size(), totalJobsCount);
    }

    private ScanEntity buildScan(int scanCount) {
        ScanEntity scan = new ScanEntity();
        scan.setLastRun(LocalDateTime.now());
        scan.setScanCount(scanCount);
        scan.setServiceScanned("Dice");
        return scan;
    }

    private JobListingsEntity toJobEntity(JobListing listing, ScanEntity savedScan) {
        JobListingsEntity entity = new JobListingsEntity();

        // IMPORTANT: do not copy the primary key onto an IDENTITY entity
        BeanUtils.copyProperties(listing, entity, "id");

        // Ensure Hibernate treats this as a NEW row (INSERT)
        entity.setId(null);

        entity.setScan(savedScan);
        return entity;
    }

    public List<JobListing> getAllListings() {
        return jobListingsRepository.findAllByOrderByPostedDateDesc().stream()
                .map(jobMapper::toListing)
                .toList();
    }

    @Tool(description = "Retrieves the 'Dice Id' from a Dice job details page URL")
    public String fetchDiceId(String url) {
        try {
            String html = restTemplate.getForObject(url, String.class);
            if (html == null) return "Could not fetch page content";

            Pattern pattern = Pattern.compile("Dice Id:\\s*<!--\\s*-->\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                logger.info("Found Dice Id: {}", matcher.group(1));
                return matcher.group(1).trim();
            }
            return "Dice Id not found on page";
        } catch (Exception e) {
            return "Error retrieving Dice Id: " + e.getMessage();
        }
    }
}