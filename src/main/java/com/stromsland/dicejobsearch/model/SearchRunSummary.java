package com.stromsland.dicejobsearch.model;

public record SearchRunSummary(
        String Query,
        int newJobsCount,
        long totalJobsCount
) {
}
