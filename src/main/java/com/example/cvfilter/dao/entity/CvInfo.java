package com.example.cvfilter.dao.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cv_info")
public class CvInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "job_offer_id")
    private Long jobOfferId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "cv_path")
    private String cvPath;

    private String name;
    private String email;
    private String phone;

    @Column(length = 2000)
    private String description;

    @Column(length = 5000)
    private String skills;

    @Column(length = 2000)
    private String experience;

    @Column(length = 5000)
    private String education;

    @Column(name = "extracted_at")
    private LocalDateTime extractedAt;

    @Enumerated(EnumType.STRING)
    private CvStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_offer_id", insertable = false, updatable = false)
    private JobOffer jobOffer;

    private String YearsOfExperience;

    private String highestDegree;

    public CvInfo() {}

    public CvInfo(Long userId, Long jobOfferId, Long companyId, String cvPath) {
        this.userId = userId;
        this.jobOfferId = jobOfferId;
        this.companyId = companyId;
        this.cvPath = cvPath;
        this.extractedAt = LocalDateTime.now();
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getJobOfferId() {
        return jobOfferId;
    }

    public void setJobOfferId(Long jobOfferId) {
        this.jobOfferId = jobOfferId;
    }

    public String getCvPath() {
        return cvPath;
    }

    public void setCvPath(String cvPath) {
        this.cvPath = cvPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public LocalDateTime getExtractedAt() {
        return extractedAt;
    }

    public void setExtractedAt(LocalDateTime extractedAt) {
        this.extractedAt = extractedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JobOffer getJobOffer() {
        return jobOffer;
    }

    public void setJobOffer(JobOffer jobOffer) {
        this.jobOffer = jobOffer;
    }

    public String getYearsOfExperience() {
        return YearsOfExperience;
    }

    public void setYearsOfExperience(String yearsOfExperience) {
        YearsOfExperience = yearsOfExperience;
    }

    public String getHighestDegree() {
        return highestDegree;
    }

    public void setHighestDegree(String highestDegree) {
        this.highestDegree = highestDegree;
    }

    public CvStatus getStatus() {
        return status;
    }

    public void setStatus(CvStatus status) {
        this.status = status;
    }


}