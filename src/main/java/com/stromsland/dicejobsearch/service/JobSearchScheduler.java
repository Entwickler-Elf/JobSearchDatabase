package com.stromsland.dicejobsearch.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class JobSearchScheduler {

    private final JobSearchService jobSearchService;
    private final SearchUpdateBroadcaster broadcaster;

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Value("${dice.search.default-query:software engineer}")
    private String query;

    @Value("${dice.search.time-zone:UTC}")
    private String timeZone;

    public JobSearchScheduler(JobSearchService jobSearchService, SearchUpdateBroadcaster broadcaster) {
        this.jobSearchService = jobSearchService;
        this.broadcaster = broadcaster;
    }

    @Async
    @Scheduled(cron = "${dice.search.cron:0 0 0,12 * * *}", zone = "${dice.search.time-zone:UTC}")
    public void runSearchOnTimer() {
        if (!running.compareAndSet(false, true)) {
            return; // prevent overlapping runs
        }

        long startNs = System.nanoTime();
        try {
            var results = jobSearchService.searchJobs(query);
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            broadcaster.completed(query, results == null ? 0 : results.size(), elapsedMs);
        } catch (Exception ex) {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            broadcaster.failed(query, ex.getMessage(), elapsedMs);
        } finally {
            running.set(false);
        }
    }
}