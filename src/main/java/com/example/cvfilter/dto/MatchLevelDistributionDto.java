package com.example.cvfilter.dto;

public class MatchLevelDistributionDto {
    private long excellent;
    private long good;
    private long fair;
    private long poor;

    public MatchLevelDistributionDto() {}

    public MatchLevelDistributionDto(long excellent, long good, long fair, long poor) {
        this.excellent = excellent;
        this.good = good;
        this.fair = fair;
        this.poor = poor;
    }

    public long getExcellent() { return excellent; }
    public void setExcellent(long excellent) { this.excellent = excellent; }

    public long getGood() { return good; }
    public void setGood(long good) { this.good = good; }

    public long getFair() { return fair; }
    public void setFair(long fair) { this.fair = fair; }

    public long getPoor() { return poor; }
    public void setPoor(long poor) { this.poor = poor; }
}
