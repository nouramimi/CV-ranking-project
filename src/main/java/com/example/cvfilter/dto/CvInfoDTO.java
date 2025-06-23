package com.example.cvfilter.dto;

import com.example.cvfilter.dao.entity.CvInfo;

import java.time.LocalDateTime;

public class CvInfoDTO {
    private Long id;
    private Long companyId;
    private String cvPath;
    private String description;
    private String education;
    private String email;  // From CV extraction
    private String experience;
    private LocalDateTime extractedAt;
    private Long jobOfferId;
    private String name;  // From CV extraction
    private String phone;
    private String skills;
    private Long userId;
    private String highestDegree;

    private String userUsername;   // From User entity
    private String userEmail;      // From User entity

    public CvInfoDTO(CvInfo cvInfo) {
        this.id = cvInfo.getId();
        this.companyId = cvInfo.getCompanyId();
        this.cvPath = cvInfo.getCvPath();
        this.description = cvInfo.getDescription();
        this.education = cvInfo.getEducation();
        this.email = cvInfo.getEmail();
        this.experience = cvInfo.getExperience();
        this.extractedAt = cvInfo.getExtractedAt();
        this.jobOfferId = cvInfo.getJobOfferId();
        this.name = cvInfo.getName();
        this.phone = cvInfo.getPhone();
        this.skills = cvInfo.getSkills();
        this.userId = cvInfo.getUserId();
        this.highestDegree=cvInfo.getHighestDegree();

        if (cvInfo.getUser() != null) {
            this.userUsername = cvInfo.getUser().getUsername();
            this.userEmail = cvInfo.getUser().getEmail();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getCvPath() {
        return cvPath;
    }

    public String getDescription() {
        return description;
    }

    public String getEducation() {
        return education;
    }

    public String getEmail() {
        return email;
    }

    public String getExperience() {
        return experience;
    }

    public LocalDateTime getExtractedAt() {
        return extractedAt;
    }

    public Long getJobOfferId() {
        return jobOfferId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getSkills() {
        return skills;
    }

    public Long getUserId() {
        return userId;
    }

    public String getHighestDegree() {
        return highestDegree;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public String getUserEmail() {
        return userEmail;
    }
}