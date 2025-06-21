package com.example.cvfilter.dto;

import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.entity.JobOffer;
import java.time.LocalDate;
import java.util.List;

public class JobOfferWithCompanyDTO {
    private Long id;
    private Long companyId;
    private String title;
    private String description;
    private String detailedDescription;
    private String location;
    private String department;
    private JobOffer.EmploymentType employmentType;
    private LocalDate postingDate;
    private LocalDate closingDate;
    private Double minSalary;
    private Double maxSalary;
    private String salaryCurrency;
    private Integer yearsOfExperienceRequired;
    private String contactEmail;
    private String contactPhone;
    private Boolean isActive;
    private List<String> skills;
    private List<String> technologies;

    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyLogo;

    public JobOfferWithCompanyDTO(JobOffer jobOffer, Company company) {
        this.id = jobOffer.getId();
        this.companyId = jobOffer.getCompanyId();
        this.title = jobOffer.getTitle();
        this.description = jobOffer.getDescription();
        this.detailedDescription = jobOffer.getDetailedDescription();
        this.location = jobOffer.getLocation();
        this.department = jobOffer.getDepartment();
        this.employmentType = jobOffer.getEmploymentType();
        this.postingDate = jobOffer.getPostingDate();
        this.closingDate = jobOffer.getClosingDate();
        this.minSalary = jobOffer.getMinSalary();
        this.maxSalary = jobOffer.getMaxSalary();
        this.salaryCurrency = jobOffer.getSalaryCurrency();
        this.yearsOfExperienceRequired = jobOffer.getYearsOfExperienceRequired();
        this.contactEmail = jobOffer.getContactEmail();
        this.contactPhone = jobOffer.getContactPhone();
        this.isActive = jobOffer.getIsActive();
        this.skills = jobOffer.getSkills();
        this.technologies = jobOffer.getTechnologies();

        if (company != null) {
            this.companyName = company.getName();
            this.companyAddress = company.getAddress();
            this.companyPhone = company.getPhone();
            this.companyLogo = company.getLogo();
        }
    }

    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDetailedDescription() { return detailedDescription; }
    public String getLocation() { return location; }
    public String getDepartment() { return department; }
    public JobOffer.EmploymentType getEmploymentType() { return employmentType; }
    public LocalDate getPostingDate() { return postingDate; }
    public LocalDate getClosingDate() { return closingDate; }
    public Double getMinSalary() { return minSalary; }
    public Double getMaxSalary() { return maxSalary; }
    public String getSalaryCurrency() { return salaryCurrency; }
    public Integer getYearsOfExperienceRequired() { return yearsOfExperienceRequired; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public Boolean getIsActive() { return isActive; }
    public List<String> getSkills() { return skills; }
    public List<String> getTechnologies() { return technologies; }

    public String getCompanyName() { return companyName; }
    public String getCompanyAddress() { return companyAddress; }
    public String getCompanyPhone() { return companyPhone; }
    public String getCompanyLogo() { return companyLogo; }
}