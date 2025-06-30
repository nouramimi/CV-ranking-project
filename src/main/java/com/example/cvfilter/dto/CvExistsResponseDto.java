package com.example.cvfilter.dto;

public class CvExistsResponseDto {
    private Long userId;
    private Long jobOfferId;
    private boolean exists;

    public CvExistsResponseDto() {}

    public CvExistsResponseDto(Long userId, Long jobOfferId, boolean exists) {
        this.userId = userId;
        this.jobOfferId = jobOfferId;
        this.exists = exists;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(Long jobOfferId) { this.jobOfferId = jobOfferId; }

    public boolean isExists() { return exists; }
    public void setExists(boolean exists) { this.exists = exists; }
}
