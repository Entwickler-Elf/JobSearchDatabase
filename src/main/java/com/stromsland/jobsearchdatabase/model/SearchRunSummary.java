package com.stromsland.jobsearchdatabase.model;

public record SearchRunSummary(
        String Query,
        int newJobsCount,
        long totalJobsCount
) {
}
