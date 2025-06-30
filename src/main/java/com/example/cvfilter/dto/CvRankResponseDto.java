package com.example.cvfilter.dto;

public class CvRankResponseDto {
    private Long userId;
    private Long jobOfferId;
    private Long rank;

    public CvRankResponseDto() {}

    public CvRankResponseDto(Long userId, Long jobOfferId, Long rank) {
        this.userId = userId;
        this.jobOfferId = jobOfferId;
        this.rank = rank;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(Long jobOfferId) { this.jobOfferId = jobOfferId; }

    public Long getRank() { return rank; }
    public void setRank(Long rank) { this.rank = rank; }
}