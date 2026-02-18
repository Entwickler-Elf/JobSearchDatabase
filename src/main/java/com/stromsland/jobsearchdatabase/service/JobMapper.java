package com.stromsland.jobsearchdatabase.service;

import com.stromsland.jobsearchdatabase.model.DiceJobEntity;
import com.stromsland.jobsearchdatabase.model.JobListing;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public JobListing toListing(DiceJobEntity entity) {
        return new JobListing(
                entity.getId(),
                entity.getTitle(),
                entity.getSummary(),
                entity.getCompanyName(),
                entity.getJobLocation(),
                entity.getDetailsPageUrl(),
                entity.getCompanyPageUrl(),
                entity.getSalary(),
                entity.getEmploymentType(),
                entity.getWorkplaceTypes(),
                entity.getPostedDate(),
                entity.isEasyApply(),
                entity.isApplied(),
                entity.isRejected(),
                entity.getDiceId()
        );
    }
}