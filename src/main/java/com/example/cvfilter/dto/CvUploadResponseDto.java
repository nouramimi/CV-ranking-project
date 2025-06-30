package com.example.cvfilter.dto;

public class CvUploadResponseDto {

    private String message;
    private String cvPath;
    private Long cvInfoId;

    public CvUploadResponseDto() {}

    public CvUploadResponseDto(String message, String cvPath, Long cvInfoId) {
        this.message = message;
        this.cvPath = cvPath;
        this.cvInfoId = cvInfoId;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }

    public Long getCvInfoId() { return cvInfoId; }
    public void setCvInfoId(Long cvInfoId) { this.cvInfoId = cvInfoId; }
}