package com.example.cvfilter.dto;

public class CvComparisonItemDto {
    private Long userId;
    private Double finalScore;
    private Double jobMatchScore;
    private Double organizationScore;
    private String matchLevel;
    private Long rank;

    public CvComparisonItemDto() {}

    public CvComparisonItemDto(Long userId, Double finalScore, Double jobMatchScore,
                               Double organizationScore, String matchLevel, Long rank) {
        this.userId = userId;
        this.finalScore = finalScore;
        this.jobMatchScore = jobMatchScore;
        this.organizationScore = organizationScore;
        this.matchLevel = matchLevel;
        this.rank = rank;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

    public Double getJobMatchScore() { return jobMatchScore; }
    public void setJobMatchScore(Double jobMatchScore) { this.jobMatchScore = jobMatchScore; }

    public Double getOrganizationScore() { return organizationScore; }
    public void setOrganizationScore(Double organizationScore) { this.organizationScore = organizationScore; }

    public String getMatchLevel() { return matchLevel; }
    public void setMatchLevel(String matchLevel) { this.matchLevel = matchLevel; }

    public Long getRank() { return rank; }
    public void setRank(Long rank) { this.rank = rank; }
}