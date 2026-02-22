package com.stromsland.jobsearchdatabase.controller;

import com.stromsland.jobsearchdatabase.service.JobSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/job-search")
public class JobSearchAdminController {

    private final JobSearchService jobSearchService;

    public JobSearchAdminController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }

    @PostMapping("/run")
    public ResponseEntity<JobSearchService.SearchRunSummary> runNow(
            @RequestParam(defaultValue = "java") String query
    ) {
        String query1 = """
                  I am looking for a java software developer position. It may contain Angular, it may contain
                   Angular and React but it will not include React without the presence of Angular.
                  ** The position will require experience any of  java, spring boot, postgreSQL.
                  ** It must be within 120 miles of Huntsville, AL OR remote.
                  ** Positions that are in Huntsville, AL must not require active security clearance or active top secret security clearance in order to apply.
                  ** Positions in or within 120 miles to Huntsville may be onsite or hybrid or remote.
                  ** Positions that are not within 120 miles of Huntsville must be fully remote.
                  ** W2 Contract work is preferred, full-time and part-time work are acceptable.
                  Report possible positions that have a posted date no more than 31 days old. "
                """;
        return ResponseEntity.ok(jobSearchService.runSearchJobs(query1));
    }
}