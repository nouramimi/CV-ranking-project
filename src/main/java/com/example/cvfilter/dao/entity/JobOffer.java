package com.example.cvfilter.dao.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
public class JobOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id")
    private Long companyId;

    private String title;
    private String description;

    @Column(length = 1000)
    private String detailedDescription;

    private String location;
    private String department;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    private LocalDate postingDate;
    private LocalDate closingDate;

    private Double minSalary;
    private Double maxSalary;
    private String salaryCurrency;

    private Integer yearsOfExperienceRequired;

    private String contactEmail;
    private String contactPhone;

    private Boolean isActive;

    @ElementCollection
    @CollectionTable(name = "job_offer_skills", joinColumns = @JoinColumn(name = "job_offer_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "job_offer_technologies", joinColumns = @JoinColumn(name = "job_offer_id"))
    @Column(name = "technology")
    private List<String> technologies = new ArrayList<>();

    @OneToMany(mappedBy = "jobOffer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CvInfo> cvInfos = new ArrayList<>();

    public JobOffer() {}

    public JobOffer(Long companyId, String title, String description, String location) {
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.location = location;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Boolean getActive() {
        return isActive;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDetailedDescription() { return detailedDescription; }
    public void setDetailedDescription(String detailedDescription) { this.detailedDescription = detailedDescription; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }

    public LocalDate getPostingDate() { return postingDate; }
    public void setPostingDate(LocalDate postingDate) { this.postingDate = postingDate; }

    public LocalDate getClosingDate() { return closingDate; }
    public void setClosingDate(LocalDate closingDate) { this.closingDate = closingDate; }

    public Double getMinSalary() { return minSalary; }
    public void setMinSalary(Double minSalary) { this.minSalary = minSalary; }

    public Double getMaxSalary() { return maxSalary; }
    public void setMaxSalary(Double maxSalary) { this.maxSalary = maxSalary; }

    public String getSalaryCurrency() { return salaryCurrency; }
    public void setSalaryCurrency(String salaryCurrency) { this.salaryCurrency = salaryCurrency; }

    public Integer getYearsOfExperienceRequired() { return yearsOfExperienceRequired; }
    public void setYearsOfExperienceRequired(Integer yearsOfExperienceRequired) { this.yearsOfExperienceRequired = yearsOfExperienceRequired; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public Boolean getIsActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public List<String> getTechnologies() { return technologies; }
    public void setTechnologies(List<String> technologies) { this.technologies = technologies; }

    public List<CvInfo> getCvInfos() {
        return cvInfos;
    }

    public void setCvInfos(List<CvInfo> cvInfos) {
        this.cvInfos = cvInfos;
    }



    public enum EmploymentType {
        FULL_TIME,
        PART_TIME,
        CONTRACT,
        TEMPORARY,
        INTERNSHIP,
        VOLUNTEER
    }

    public enum EducationLevel {
        HIGH_SCHOOL,
        ASSOCIATE,
        BACHELOR,
        MASTER,
        PHD,
        NONE_SPECIFIED
    }
}