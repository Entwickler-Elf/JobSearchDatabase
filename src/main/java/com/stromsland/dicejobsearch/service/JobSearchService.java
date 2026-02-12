package com.stromsland.dicejobsearch.service;


import com.stromsland.dicejobsearch.model.DiceJobEntity;
import com.stromsland.dicejobsearch.model.JobListing;
import com.stromsland.dicejobsearch.repository.DiceJobRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
@Service
public class JobSearchService {

    public record SearchRunSummary(
            String Query,
            int newJobsCount,
            long totalJobsCount
    ) { }

    private final ChatClient chatClient;
    private final RestTemplate restTemplate;
    // Use TypeReference to properly handle the List of Records
    private final BeanOutputConverter<List<JobListing>> outputConverter;
    private final JobMapper jobMapper;
    private final DiceJobRepository diceJobRepository;

    private final String systemPrompt = """
    You are a job search assistant.

    IMPORTANT TOOL SCHEMA RULES (MUST FOLLOW):
    - When calling the Dice tool 'search_jobs', DO NOT request 'jobLocation' in the 'fields' list.
    - The allowed location field is 'jobLocation.displayName' (use that exact string).

    WORKFLOW:
    1. Search for jobs using 'search_jobs' with the user's query.
       - When you call 'search_jobs', include fields that match the tool schema, including:
         id, title, summary, companyName, jobLocation.displayName, detailsPageUrl, companyPageUrl,
         salary, employmentType, workplaceTypes, postedDate, easyApply
    2. For the FIRST result, use 'fetchDiceId' with the 'detailsPageUrl'.
    3. Include that ID in the 'diceId' field of your response.

    DATA MAPPING (TO YOUR JSON OUTPUT):
    - Map Dice 'jobLocation.displayName' -> output field 'jobLocation'
    - Map 'companyName' -> 'companyName'
    - Map 'employmentType' -> 'employmentType'
    """;

    public JobSearchService(ChatClient.Builder chatClientBuilder, ToolCallbackProvider mcpTools, DiceJobRepository diceJobRepository, JobMapper jobMapper) {
        this.jobMapper = jobMapper;
        this.diceJobRepository = diceJobRepository;
        this.restTemplate = new RestTemplate();

        // Correct initialization using Spring's ParameterizedTypeReference
        this.outputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<JobListing>>() {
        });

        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultToolCallbacks(mcpTools.getToolCallbacks())
                .defaultTools(this)
                .build();

    }

    /**
     * Use this for scheduled runs / status reporting.
     * It performs the search + persistence and returns how many NEW jobs were saved.
     */
    public SearchRunSummary runSearchJobs(String query) {
        System.out.println("invoking chat");
        StopWatch stopWatch = new StopWatch("chat");
        stopWatch.start("chat");

        List<JobListing> listings = chatClient.prompt()
                .user(query)
                .call()
                .entity(outputConverter);

        stopWatch.stop();
        System.out.println("chat response " + stopWatch.prettyPrint());

        System.out.println("begin saving to database");
        stopWatch.start("save in database");

        int newJobsCount = 0;
        if (listings != null) {
            List<DiceJobEntity> entities = listings.stream()
                    .filter(listing -> !diceJobRepository.existsById(listing.id()))
                    .map(listing -> {
                        DiceJobEntity entity = new DiceJobEntity();
                        BeanUtils.copyProperties(listing, entity);
                        return entity;
                    }).toList();

            newJobsCount = entities.size();
            if (!entities.isEmpty()) {
                diceJobRepository.saveAll(entities);
            }
        }

        stopWatch.stop();
        System.out.println("finished saving to the database " + stopWatch.prettyPrint());

        long totalJobsCount = diceJobRepository.count();
        return new SearchRunSummary(query, newJobsCount, totalJobsCount);
    }

    public List<JobListing> searchJobs(String query) {
        runSearchJobs(query);
        return getAllListings();
    }

    public List<JobListing> getAllListings() {
        return diceJobRepository.findAllByOrderByPostedDateDesc().stream()
                .map(jobMapper::toListing)
                .toList();
    }
 
    @Tool(description = "Retrieves the 'Dice Id' from a Dice job details page URL")
    public String fetchDiceId(String url) {
        try {
            String html = restTemplate.getForObject(url, String.class);
            if (html == null) return "Could not fetch page content";
            
            // Search for "Dice Id:" label and capture value to the right
            Pattern pattern = Pattern.compile("Dice Id:\\s*<!--\\s*-->\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
            return "Dice Id not found on page";
        } catch (Exception e) {
            return "Error retrieving Dice Id: " + e.getMessage();
        }
    }
}