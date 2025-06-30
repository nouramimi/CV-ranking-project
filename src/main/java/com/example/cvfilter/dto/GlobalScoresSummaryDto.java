package com.example.cvfilter.dto;

public class GlobalScoresSummaryDto {
    private long totalCvs;
    private double avgFinalScore;
    private double avgJobMatchScore;
    private double avgOrganizationScore;
    private long excellentMatches;
    private long goodMatches;
    private MatchLevelDistributionDto matchLevelDistribution;

    public GlobalScoresSummaryDto() {}

    public GlobalScoresSummaryDto(long totalCvs, double avgFinalScore, double avgJobMatchScore,
                                  double avgOrganizationScore, long excellentMatches, long goodMatches,
                                  MatchLevelDistributionDto matchLevelDistribution) {
        this.totalCvs = totalCvs;
        this.avgFinalScore = avgFinalScore;
        this.avgJobMatchScore = avgJobMatchScore;
        this.avgOrganizationScore = avgOrganizationScore;
        this.excellentMatches = excellentMatches;
        this.goodMatches = goodMatches;
        this.matchLevelDistribution = matchLevelDistribution;
    }

    public long getTotalCvs() { return totalCvs; }
    public void setTotalCvs(long totalCvs) { this.totalCvs = totalCvs; }

    public double getAvgFinalScore() { return avgFinalScore; }
    public void setAvgFinalScore(double avgFinalScore) { this.avgFinalScore = avgFinalScore; }

    public double getAvgJobMatchScore() { return avgJobMatchScore; }
    public void setAvgJobMatchScore(double avgJobMatchScore) { this.avgJobMatchScore = avgJobMatchScore; }

    public double getAvgOrganizationScore() { return avgOrganizationScore; }
    public void setAvgOrganizationScore(double avgOrganizationScore) { this.avgOrganizationScore = avgOrganizationScore; }

    public long getExcellentMatches() { return excellentMatches; }
    public void setExcellentMatches(long excellentMatches) { this.excellentMatches = excellentMatches; }

    public long getGoodMatches() { return goodMatches; }
    public void setGoodMatches(long goodMatches) { this.goodMatches = goodMatches; }

    public MatchLevelDistributionDto getMatchLevelDistribution() { return matchLevelDistribution; }
    public void setMatchLevelDistribution(MatchLevelDistributionDto matchLevelDistribution) { this.matchLevelDistribution = matchLevelDistribution; }
}