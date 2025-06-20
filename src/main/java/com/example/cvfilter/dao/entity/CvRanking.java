package com.example.cvfilter.dao.entity;

import java.time.LocalDateTime;

public class CvRanking {
    private CvInfo cvInfo;
    private double similarityScore;
    private int rank;
    private LocalDateTime rankedAt;

    public CvRanking() {
    }

    public CvRanking(CvInfo cvInfo, double similarityScore) {
        this.cvInfo = cvInfo;
        this.similarityScore = similarityScore;
        this.rankedAt = LocalDateTime.now();
    }

    public CvInfo getCvInfo() {
        return cvInfo;
    }

    public void setCvInfo(CvInfo cvInfo) {
        this.cvInfo = cvInfo;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public LocalDateTime getRankedAt() {
        return rankedAt;
    }

    public void setRankedAt(LocalDateTime rankedAt) {
        this.rankedAt = rankedAt;
    }

    public double getSimilarityPercentage() {
        return Math.round(similarityScore * 100.0 * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return "CvRanking{" +
                "userId=" + (cvInfo != null ? cvInfo.getUserId() : null) +
                ", jobOfferId=" + (cvInfo != null ? cvInfo.getJobOfferId() : null) +
                ", similarityScore=" + similarityScore +
                ", rank=" + rank +
                ", rankedAt=" + rankedAt +
                '}';
    }
}