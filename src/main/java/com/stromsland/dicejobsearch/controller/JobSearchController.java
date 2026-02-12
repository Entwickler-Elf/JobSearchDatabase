package com.stromsland.dicejobsearch.controller;

import com.stromsland.dicejobsearch.model.JobListing;
import com.stromsland.dicejobsearch.service.JobSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
 
@RestController
public class JobSearchController {

    public record SearchResponse(List<JobListing> results) {}
    private final JobSearchService jobSearchService;
 
    public JobSearchController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }

    @GetMapping("/api")
    public SearchResponse jobListings() {
        List<JobListing> results = jobSearchService.getAllListings();

        return new SearchResponse(results);
    }
}