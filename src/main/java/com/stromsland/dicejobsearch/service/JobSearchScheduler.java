package com.stromsland.dicejobsearch.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class JobSearchScheduler {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JobSearchScheduler.class);

    private final JobSearchService jobSearchService;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Value("${dice.search.default-query:software engineer}")
    private String query;

    @Value("${dice.search.time-zone:America/Chicago}")
    private String timeZone;

    public JobSearchScheduler(JobSearchService jobSearchService) {
        logger.info("Initializing JobSearchScheduler with query: {}", query);
        this.jobSearchService = jobSearchService;
    }

    @Async
    @Scheduled(
            cron = "${dice.search.cron:0 5 17 * * *}",
            zone = "${dice.search.time-zone:America/Chicago}")
    public void runSearchOnTimer() {
        if (!running.compareAndSet(false, true)) {
            return; // prevent overlapping runs
        }

        long startNs = System.nanoTime();
        try {
            var summary = jobSearchService.runSearchJobs(query);
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        } catch (Exception ex) {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
        } finally {
            running.set(false);
            logger.info("Finished running search jobs elapsed time{}", (System.nanoTime() - startNs) / 1_000_000);
        }
    }
}