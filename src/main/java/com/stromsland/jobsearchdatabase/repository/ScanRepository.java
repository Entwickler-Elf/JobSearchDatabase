package com.stromsland.jobsearchdatabase.repository;

import com.stromsland.jobsearchdatabase.model.ScanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanRepository extends JpaRepository<ScanEntity, Long> {
}