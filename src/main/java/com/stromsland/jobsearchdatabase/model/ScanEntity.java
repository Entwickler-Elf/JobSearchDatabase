package com.stromsland.jobsearchdatabase.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "scans")
public class ScanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "last_run", nullable = false)
    LocalDateTime lastRun;

    @Column(name = "scan_count", nullable = false)
    Integer scanCount;

    @Column(name = "service_scanned", nullable = false)
    String serviceScanned;

    @OneToMany(mappedBy = "scan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobListingsEntity> diceJobs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getLastRun() {
        return lastRun;
    }

    public void setLastRun(LocalDateTime lastRun) {
        this.lastRun = lastRun;
    }

    public Integer getScanCount() {
        return scanCount;
    }

    public void setScanCount(Integer scanCount) {
        this.scanCount = scanCount;
    }

    public String getServiceScanned() {
        return serviceScanned;
    }

    public void setServiceScanned(String serviceScanned) {
        this.serviceScanned = serviceScanned;
    }

    public List<JobListingsEntity> getDiceJobs() {
        return diceJobs;
    }

    public void setDiceJobs(List<JobListingsEntity> diceJobs) {
        this.diceJobs = diceJobs;
    }
}