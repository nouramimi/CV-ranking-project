package com.example.cvfilter.dto;

import com.example.cvfilter.dao.entity.CvInfo;

import java.time.LocalDateTime;

public class CvInfoDTO {
    private Long id;
    private Long companyId;
    private String cvPath;
    private String description;
    private String education;
    private String email;
    private String experience;
    private LocalDateTime extractedAt;
    private Long jobOfferId;
    private String name;
    private String phone;
    private String skills;
    private Long userId;

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
    }
}
