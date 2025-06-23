package com.example.cvfilter.dto;


import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dao.entity.JobOffer.EmploymentType;
import java.time.LocalDate;
import java.util.List;

public class JobOfferDTO {
    private Long id;
    private Long companyId;
    private String title;
    private String description;
    private String detailedDescription;
    private String location;
    private String department;
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
    private List<String> skills;
    private List<String> technologies;
    private JobOffer.EducationLevel requiredDegree;

    public JobOfferDTO() {}

    public JobOfferDTO(JobOffer jobOffer) {
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
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(LocalDate postingDate) {
        this.postingDate = postingDate;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public void setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
    }

    public Double getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(Double minSalary) {
        this.minSalary = minSalary;
    }

    public Double getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(Double maxSalary) {
        this.maxSalary = maxSalary;
    }

    public String getSalaryCurrency() {
        return salaryCurrency;
    }

    public void setSalaryCurrency(String salaryCurrency) {
        this.salaryCurrency = salaryCurrency;
    }

    public Integer getYearsOfExperienceRequired() {
        return yearsOfExperienceRequired;
    }

    public void setYearsOfExperienceRequired(Integer yearsOfExperienceRequired) {
        this.yearsOfExperienceRequired = yearsOfExperienceRequired;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getTechnologies() {
        return technologies;
    }

    public void setTechnologies(List<String> technologies) {
        this.technologies = technologies;
    }

    public JobOffer.EducationLevel getRequiredDegree() {
        return requiredDegree;
    }

    public void setRequiredDegree(JobOffer.EducationLevel requiredDegree) {
        this.requiredDegree = requiredDegree;
    }
}
