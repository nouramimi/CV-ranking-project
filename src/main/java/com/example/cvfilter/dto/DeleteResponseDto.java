package com.example.cvfilter.dto;

public class DeleteResponseDto {
    private Long userId;
    private Long jobOfferId;
    private boolean success;
    private String message;

    public DeleteResponseDto() {}

    public DeleteResponseDto(Long userId, Long jobOfferId, boolean success, String message) {
        this.userId = userId;
        this.jobOfferId = jobOfferId;
        this.success = success;
        this.message = message;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(Long jobOfferId) { this.jobOfferId = jobOfferId; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
