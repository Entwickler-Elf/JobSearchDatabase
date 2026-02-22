package com.stromsland.jobsearchdatabase.repository;

import com.stromsland.jobsearchdatabase.model.JobListingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobListingsRepository extends JpaRepository<JobListingsEntity, Long> {
    List<JobListingsEntity> findAllByOrderByPostedDateDesc();
}
