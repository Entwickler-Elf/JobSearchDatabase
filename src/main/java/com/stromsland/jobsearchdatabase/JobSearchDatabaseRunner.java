package com.stromsland.jobsearchdatabase;

import com.stromsland.jobsearchdatabase.model.JobListing;
import com.stromsland.jobsearchdatabase.service.JobSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "jobsearch.runner.enabled", havingValue = "true")
public class JobSearchDatabaseRunner implements CommandLineRunner {

    private final JobSearchService jobSearchService;
    private final String defaultQuery;

    public JobSearchDatabaseRunner(JobSearchService jobSearchService,
                                   @Value("${jobsearch.default-query}") String defaultQuery) {
        this.jobSearchService = jobSearchService;
        this.defaultQuery = defaultQuery;
    }

    @Override
    public void run(String... args) {
        System.out.println("Running automated job search...");
        List<JobListing> response = jobSearchService.searchJobs(defaultQuery);
        System.out.println("--- Search Results ---");
        for (JobListing listing : response) {
            System.out.println(listing);
        }

    }
}