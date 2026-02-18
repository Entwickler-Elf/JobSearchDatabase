package com.stromsland.jobsearchdatabase.service;

import com.stromsland.jobsearchdatabase.model.DiceJobEntity;
import com.stromsland.jobsearchdatabase.model.JobListing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobMapperTest {

    private JobMapper jobMapper;

    @BeforeEach
    void setUp() {
        jobMapper = new JobMapper();
    }

    @Test
    void toListing_ShouldMapAllFieldsCorrectly() {
        // Arrange
        DiceJobEntity entity = new DiceJobEntity();
        entity.setId("test-id");
        entity.setTitle("Software Engineer");
        entity.setSummary("Great job");
        entity.setCompanyName("Tech Corp");
        entity.setJobLocation("Remote");
        entity.setDetailsPageUrl("http://details.com");
        entity.setCompanyPageUrl("http://company.com");
        entity.setSalary("$100k");
        entity.setEmploymentType("Full-time");
        entity.setWorkplaceTypes("Remote");
        entity.setPostedDate("2026-01-20");
        entity.setEasyApply(true);
        entity.setDiceId("dice-123");

        // Act
        JobListing listing = jobMapper.toListing(entity);

        // Assert
        assertNotNull(listing);
        assertEquals(entity.getId(), listing.id());
        assertEquals(entity.getTitle(), listing.title());
        assertEquals(entity.getSummary(), listing.summary());
        assertEquals(entity.getCompanyName(), listing.companyName());
        assertEquals(entity.getJobLocation(), listing.jobLocation());
        assertEquals(entity.getDetailsPageUrl(), listing.detailsPageUrl());
        assertEquals(entity.getCompanyPageUrl(), listing.companyPageUrl());
        assertEquals(entity.getSalary(), listing.salary());
        assertEquals(entity.getEmploymentType(), listing.employmentType());
        assertEquals(entity.getWorkplaceTypes(), listing.workplaceTypes());
        assertEquals(entity.getPostedDate(), listing.postedDate());
        assertEquals(entity.isEasyApply(), listing.easyApply());
        assertEquals(entity.getDiceId(), listing.diceId());
    }
}