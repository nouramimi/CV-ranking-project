package com.example.cvfilter.dto;

import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dao.entity.CvScores;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class JobOfferWithScoreDTO {
    // Job Offer fields
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
    private JobOffer.EducationLevel requiredDegree;

    private String companyName;
    private String companyAddress;
    private String companyPhone;
    private String companyLogo;

    private BigDecimal finalScore;
    private BigDecimal jobMatchScore;
    private BigDecimal organizationScore;
    private BigDecimal technicalScore;
    private BigDecimal compositeScore;
    private String matchLevel;
    private LocalDateTime scoredAt;

    public JobOfferWithScoreDTO() {}

    public JobOfferWithScoreDTO(JobOffer jobOffer, Company company) {
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
        this.requiredDegree = jobOffer.getRequiredDegree();

        if (company != null) {
            this.companyName = company.getName();
            this.companyAddress = company.getAddress();
            this.companyPhone = company.getPhone();
            this.companyLogo = company.getLogo();
        }
    }

    public JobOfferWithScoreDTO(JobOffer jobOffer, Company company, CvScores cvScores) {
        this(jobOffer, company);

        if (cvScores != null) {
            this.finalScore = cvScores.getFinalScore();
            this.jobMatchScore = cvScores.getJobMatchScore();
            this.organizationScore = cvScores.getOrganizationScore();
            this.technicalScore = cvScores.getTechnicalScore();
            this.compositeScore = cvScores.getCompositeScore();
            this.matchLevel = cvScores.getMatchLevel();
            this.scoredAt = cvScores.getProcessedAt();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

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

    public JobOffer.EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(JobOffer.EmploymentType employmentType) { this.employmentType = employmentType; }

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
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public List<String> getTechnologies() { return technologies; }
    public void setTechnologies(List<String> technologies) { this.technologies = technologies; }

    public JobOffer.EducationLevel getRequiredDegree() { return requiredDegree; }
    public void setRequiredDegree(JobOffer.EducationLevel requiredDegree) { this.requiredDegree = requiredDegree; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    public String getCompanyPhone() { return companyPhone; }
    public void setCompanyPhone(String companyPhone) { this.companyPhone = companyPhone; }

    public String getCompanyLogo() { return companyLogo; }
    public void setCompanyLogo(String companyLogo) { this.companyLogo = companyLogo; }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }

    public BigDecimal getJobMatchScore() { return jobMatchScore; }
    public void setJobMatchScore(BigDecimal jobMatchScore) { this.jobMatchScore = jobMatchScore; }

    public BigDecimal getOrganizationScore() { return organizationScore; }
    public void setOrganizationScore(BigDecimal organizationScore) { this.organizationScore = organizationScore; }

    public BigDecimal getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(BigDecimal technicalScore) { this.technicalScore = technicalScore; }

    public BigDecimal getCompositeScore() { return compositeScore; }
    public void setCompositeScore(BigDecimal compositeScore) { this.compositeScore = compositeScore; }

    public String getMatchLevel() { return matchLevel; }
    public void setMatchLevel(String matchLevel) { this.matchLevel = matchLevel; }

    public LocalDateTime getScoredAt() { return scoredAt; }
    public void setScoredAt(LocalDateTime scoredAt) { this.scoredAt = scoredAt; }
}