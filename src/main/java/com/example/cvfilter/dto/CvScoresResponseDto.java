package com.example.cvfilter.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CvScoresResponseDto {

    private Long id;
    private Long userId;
    private Long jobOfferId;
    private String cvPath;
    private BigDecimal organizationScore;
    private BigDecimal technicalScore;
    private BigDecimal compositeScore;
    private BigDecimal experienceScore;
    private BigDecimal skillsScore;
    private BigDecimal educationScore;
    private BigDecimal jobMatchScore;
    private BigDecimal skillsMatchScore;
    private BigDecimal experienceMatchScore;
    private BigDecimal educationMatchScore;
    private BigDecimal contentRelevanceScore;
    private String matchLevel;
    private String jobTitle;
    private BigDecimal finalScore;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CvScoresResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(Long jobOfferId) { this.jobOfferId = jobOfferId; }

    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }

    public BigDecimal getOrganizationScore() { return organizationScore; }
    public void setOrganizationScore(BigDecimal organizationScore) { this.organizationScore = organizationScore; }

    public BigDecimal getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(BigDecimal technicalScore) { this.technicalScore = technicalScore; }

    public BigDecimal getCompositeScore() { return compositeScore; }
    public void setCompositeScore(BigDecimal compositeScore) { this.compositeScore = compositeScore; }

    public BigDecimal getExperienceScore() { return experienceScore; }
    public void setExperienceScore(BigDecimal experienceScore) { this.experienceScore = experienceScore; }

    public BigDecimal getSkillsScore() { return skillsScore; }
    public void setSkillsScore(BigDecimal skillsScore) { this.skillsScore = skillsScore; }

    public BigDecimal getEducationScore() { return educationScore; }
    public void setEducationScore(BigDecimal educationScore) { this.educationScore = educationScore; }

    public BigDecimal getJobMatchScore() { return jobMatchScore; }
    public void setJobMatchScore(BigDecimal jobMatchScore) { this.jobMatchScore = jobMatchScore; }

    public BigDecimal getSkillsMatchScore() { return skillsMatchScore; }
    public void setSkillsMatchScore(BigDecimal skillsMatchScore) { this.skillsMatchScore = skillsMatchScore; }

    public BigDecimal getExperienceMatchScore() { return experienceMatchScore; }
    public void setExperienceMatchScore(BigDecimal experienceMatchScore) { this.experienceMatchScore = experienceMatchScore; }

    public BigDecimal getEducationMatchScore() { return educationMatchScore; }
    public void setEducationMatchScore(BigDecimal educationMatchScore) { this.educationMatchScore = educationMatchScore; }

    public BigDecimal getContentRelevanceScore() { return contentRelevanceScore; }
    public void setContentRelevanceScore(BigDecimal contentRelevanceScore) { this.contentRelevanceScore = contentRelevanceScore; }

    public String getMatchLevel() { return matchLevel; }
    public void setMatchLevel(String matchLevel) { this.matchLevel = matchLevel; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(BigDecimal finalScore) { this.finalScore = finalScore; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
