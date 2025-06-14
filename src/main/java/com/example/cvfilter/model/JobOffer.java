package com.example.cvfilter.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class JobOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(length = 1000)
    private String detailedDescription;

    private String location;
    private String department;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType; // FULL_TIME, PART_TIME, CONTRACT, etc.

    private LocalDate postingDate;
    private LocalDate closingDate;

    private Double minSalary;
    private Double maxSalary;
    private String salaryCurrency;

    private Integer yearsOfExperienceRequired;

    private String contactEmail;
    private String contactPhone;

    private Boolean isActive;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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