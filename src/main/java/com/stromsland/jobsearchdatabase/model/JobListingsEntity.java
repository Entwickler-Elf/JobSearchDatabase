package com.stromsland.jobsearchdatabase.model;

import jakarta.persistence.*;

@Entity
@Table(name = "job_listings")
public class JobListingsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "job_location")
    private String jobLocation;

    @Column(name = "details_page_url", columnDefinition = "TEXT")
    private String detailsPageUrl;

    @Column(name = "company_page_url", columnDefinition = "TEXT")
    private String companyPageUrl;

    private String salary;

    @Column(name = "employment_type")
    private String employmentType;

    @Column(name = "workplace_types")
    private String workplaceTypes;

    @Column(name = "posted_date")
    private String postedDate;

    @Column(name = "easy_apply")
    private boolean easyApply;

    @Column(name = "dice_id")
    private String diceId;

    @Column(nullable = false)
    private boolean applied = false;

    @Column(nullable = false)
    private boolean rejected = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id")
    private ScanEntity scan;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}