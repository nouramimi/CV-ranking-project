package com.example.cvfilter.dto;

import com.example.cvfilter.dao.entity.CvInfo;
import com.example.cvfilter.dao.entity.CvScores;

import java.math.BigDecimal;
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
    private CvScoreInfo cvScores;

    public CvInfoDTO() {}

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
        this.highestDegree = cvInfo.getHighestDegree();

        if (cvInfo.getUser() != null) {
            this.userUsername = cvInfo.getUser().getUsername();
            this.userEmail = cvInfo.getUser().getEmail();
        }
    }

    public CvInfoDTO(CvInfo cvInfo, CvScores cvScores) {
        this(cvInfo);
        if (cvScores != null) {
            this.cvScores = new CvScoreInfo(cvScores);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public LocalDateTime getExtractedAt() { return extractedAt; }
    public void setExtractedAt(LocalDateTime extractedAt) { this.extractedAt = extractedAt; }

    public Long getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(Long jobOfferId) { this.jobOfferId = jobOfferId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getHighestDegree() { return highestDegree; }
    public void setHighestDegree(String highestDegree) { this.highestDegree = highestDegree; }

    public String getUserUsername() { return userUsername; }
    public void setUserUsername(String userUsername) { this.userUsername = userUsername; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public CvScoreInfo getCvScores() { return cvScores; }
    public void setCvScores(CvScoreInfo cvScores) { this.cvScores = cvScores; }

    public static class CvScoreInfo {
        private BigDecimal finalScore;
        private BigDecimal jobMatchScore;
        private BigDecimal organizationScore;
        private BigDecimal technicalScore;
        private BigDecimal compositeScore;
        private String matchLevel;
        private LocalDateTime processedAt;
        private Long rank;

        public CvScoreInfo() {}

        public CvScoreInfo(CvScores cvScores) {
            this.finalScore = cvScores.getFinalScore();
            this.jobMatchScore = cvScores.getJobMatchScore();
            this.organizationScore = cvScores.getOrganizationScore();
            this.technicalScore = cvScores.getTechnicalScore();
            this.compositeScore = cvScores.getCompositeScore();
            this.matchLevel = cvScores.getMatchLevel();
            this.processedAt = cvScores.getProcessedAt();
        }

        // Getters and Setters
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

        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

        public Long getRank() { return rank; }
        public void setRank(Long rank) { this.rank = rank; }
    }
}