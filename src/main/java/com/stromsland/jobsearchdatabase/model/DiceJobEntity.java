package com.stromsland.jobsearchdatabase.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "dice_jobs")
public class DiceJobEntity {

    @Id
    @Column(nullable = false, length = 100)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String id;

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

    // Default constructor required by JPA
    public DiceJobEntity() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getJobLocation() { return jobLocation; }
    public void setJobLocation(String jobLocation) { this.jobLocation = jobLocation; }

    public String getDetailsPageUrl() { return detailsPageUrl; }
    public void setDetailsPageUrl(String detailsPageUrl) { this.detailsPageUrl = detailsPageUrl; }

    public String getCompanyPageUrl() { return companyPageUrl; }
    public void setCompanyPageUrl(String companyPageUrl) { this.companyPageUrl = companyPageUrl; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getWorkplaceTypes() { return workplaceTypes; }
    public void setWorkplaceTypes(String workplaceTypes) { this.workplaceTypes = workplaceTypes; }

    public String getPostedDate() { return postedDate; }
    public void setPostedDate(String postedDate) { this.postedDate = postedDate; }

    public boolean isEasyApply() { return easyApply; }
    public void setEasyApply(boolean easyApply) { this.easyApply = easyApply; }

    public String getDiceId() { return diceId; }
    public void setDiceId(String diceId) { this.diceId = diceId; }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }
}