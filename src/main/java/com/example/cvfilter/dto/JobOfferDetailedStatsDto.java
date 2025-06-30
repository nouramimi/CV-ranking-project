package com.example.cvfilter.dto;

public class JobOfferDetailedStatsDto {
    private long totalCvs;
    private double avgFinalScore;
    private double avgJobMatchScore;
    private double avgOrganizationScore;
    private double maxFinalScore;
    private double minFinalScore;
    private MatchLevelDistributionDto matchLevelDistribution;

    public JobOfferDetailedStatsDto() {}

    public JobOfferDetailedStatsDto(long totalCvs, double avgFinalScore, double avgJobMatchScore,
                                    double avgOrganizationScore, double maxFinalScore, double minFinalScore,
                                    MatchLevelDistributionDto matchLevelDistribution) {
        this.totalCvs = totalCvs;
        this.avgFinalScore = avgFinalScore;
        this.avgJobMatchScore = avgJobMatchScore;
        this.avgOrganizationScore = avgOrganizationScore;
        this.maxFinalScore = maxFinalScore;
        this.minFinalScore = minFinalScore;
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

    public double getMaxFinalScore() { return maxFinalScore; }
    public void setMaxFinalScore(double maxFinalScore) { this.maxFinalScore = maxFinalScore; }

    public double getMinFinalScore() { return minFinalScore; }
    public void setMinFinalScore(double minFinalScore) { this.minFinalScore = minFinalScore; }

    public MatchLevelDistributionDto getMatchLevelDistribution() { return matchLevelDistribution; }
    public void setMatchLevelDistribution(MatchLevelDistributionDto matchLevelDistribution) { this.matchLevelDistribution = matchLevelDistribution; }
}