package com.example.cvfilter.dto;

import java.util.List;

public class CvComparisonResponseDto {
    private Long jobOfferId;
    private List<CvComparisonItemDto> cvs;
    private CvComparisonItemDto winner;

    public CvComparisonResponseDto() {}

    public CvComparisonResponseDto(Long jobOfferId, List<CvComparisonItemDto> cvs, CvComparisonItemDto winner) {
        this.jobOfferId = jobOfferId;
        this.cvs = cvs;
        this.winner = winner;
    }

    public Long getJobOfferId() { return jobOfferId; }
    public void setJobOfferId(Long jobOfferId) { this.jobOfferId = jobOfferId; }

    public List<CvComparisonItemDto> getCvs() { return cvs; }
    public void setCvs(List<CvComparisonItemDto> cvs) { this.cvs = cvs; }

    public CvComparisonItemDto getWinner() { return winner; }
    public void setWinner(CvComparisonItemDto winner) { this.winner = winner; }
}
