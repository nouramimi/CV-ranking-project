package com.example.cvfilter.dao.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cv_scores")
public class CvScores {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "job_offer_id", nullable = false)
    private Long jobOfferId;

    @Column(name = "cv_path")
    private String cvPath;

    @Column(name = "organization_score", precision = 5, scale = 2)
    private BigDecimal organizationScore;

    @Column(name = "technical_score", precision = 5, scale = 2)
    private BigDecimal technicalScore;

    @Column(name = "composite_score", precision = 5, scale = 2)
    private BigDecimal compositeScore;

    @Column(name = "experience_score", precision = 5, scale = 2)
    private BigDecimal experienceScore;

    @Column(name = "skills_score", precision = 5, scale = 2)
    private BigDecimal skillsScore;

    @Column(name = "education_score", precision = 5, scale = 2)
    private BigDecimal educationScore;

    @Column(name = "job_match_score", precision = 5, scale = 2)
    private BigDecimal jobMatchScore;

    @Column(name = "skills_match_score", precision = 5, scale = 2)
    private BigDecimal skillsMatchScore;

    @Column(name = "experience_match_score", precision = 5, scale = 2)
    private BigDecimal experienceMatchScore;

    @Column(name = "education_match_score", precision = 5, scale = 2)
    private BigDecimal educationMatchScore;

    @Column(name = "content_relevance_score", precision = 5, scale = 2)
    private BigDecimal contentRelevanceScore;

    @Column(name = "match_level", length = 20)
    private String matchLevel;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "final_score", precision = 5, scale = 2)
    private BigDecimal finalScore;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CvScores() {}

    public CvScores(Long userId, Long jobOfferId, String cvPath) {
        this.userId = userId;
        this.jobOfferId = jobOfferId;
        this.cvPath = cvPath;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(Long jobOfferId) { this.jobOfferId = jobOfferId; }

    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }

    public BigDecimal getOrganizationScore() { return organizationScore; }
    public void setOrganizationScore(Double organizationScore) {
        this.organizationScore = organizationScore != null ? BigDecimal.valueOf(organizationScore) : null;
    }

    public BigDecimal getTechnicalScore() { return technicalScore; }
    public void setTechnicalScore(Double technicalScore) {
        this.technicalScore = technicalScore != null ? BigDecimal.valueOf(technicalScore) : null;
    }

    public BigDecimal getCompositeScore() { return compositeScore; }
    public void setCompositeScore(Double compositeScore) {
        this.compositeScore = compositeScore != null ? BigDecimal.valueOf(compositeScore) : null;
    }

    public BigDecimal getExperienceScore() { return experienceScore; }
    public void setExperienceScore(Double experienceScore) {
        this.experienceScore = experienceScore != null ? BigDecimal.valueOf(experienceScore) : null;
    }

    public BigDecimal getSkillsScore() { return skillsScore; }
    public void setSkillsScore(Double skillsScore) {
        this.skillsScore = skillsScore != null ? BigDecimal.valueOf(skillsScore) : null;
    }

    public BigDecimal getEducationScore() { return educationScore; }
    public void setEducationScore(Double educationScore) {
        this.educationScore = educationScore != null ? BigDecimal.valueOf(educationScore) : null;
    }

    public BigDecimal getJobMatchScore() { return jobMatchScore; }
    public void setJobMatchScore(Double jobMatchScore) {
        this.jobMatchScore = jobMatchScore != null ? BigDecimal.valueOf(jobMatchScore) : null;
    }

    public BigDecimal getSkillsMatchScore() { return skillsMatchScore; }
    public void setSkillsMatchScore(Double skillsMatchScore) {
        this.skillsMatchScore = skillsMatchScore != null ? BigDecimal.valueOf(skillsMatchScore) : null;
    }

    public BigDecimal getExperienceMatchScore() { return experienceMatchScore; }
    public void setExperienceMatchScore(Double experienceMatchScore) {
        this.experienceMatchScore = experienceMatchScore != null ? BigDecimal.valueOf(experienceMatchScore) : null;
    }

    public BigDecimal getEducationMatchScore() { return educationMatchScore; }
    public void setEducationMatchScore(Double educationMatchScore) {
        this.educationMatchScore = educationMatchScore != null ? BigDecimal.valueOf(educationMatchScore) : null;
    }

    public BigDecimal getContentRelevanceScore() { return contentRelevanceScore; }
    public void setContentRelevanceScore(Double contentRelevanceScore) {
        this.contentRelevanceScore = contentRelevanceScore != null ? BigDecimal.valueOf(contentRelevanceScore) : null;
    }

    public String getMatchLevel() { return matchLevel; }
    public void setMatchLevel(String matchLevel) { this.matchLevel = matchLevel; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    // Score final
    public BigDecimal getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore != null ? BigDecimal.valueOf(finalScore) : null;
    }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("CvScores{id=%d, userId=%d, jobOfferId=%d, finalScore=%.2f, matchLevel='%s'}",
                id, userId, jobOfferId,
                finalScore != null ? finalScore.doubleValue() : 0.0,
                matchLevel);
    }
}