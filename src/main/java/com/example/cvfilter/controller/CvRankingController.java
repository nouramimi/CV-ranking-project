package com.example.cvfilter.controller;

import com.example.cvfilter.dao.entity.CvRanking;
import com.example.cvfilter.service.CvRankingService;
import com.example.cvfilter.service.EmailService;
import com.example.cvfilter.service.impl.CvRankingServiceInterface;
import com.example.cvfilter.service.impl.EmailServiceInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cv-ranking")
public class CvRankingController {

    private final CvRankingServiceInterface cvRankingService;
    private final EmailServiceInterface emailService;

    public CvRankingController(CvRankingServiceInterface cvRankingService, EmailServiceInterface emailService) {
        this.cvRankingService = cvRankingService;
        this.emailService = emailService;
    }

    @GetMapping("/job/{jobOfferId}/best")
    public ResponseEntity<List<CvRanking>> getBestCvsForJob(@PathVariable Long jobOfferId) {
        List<CvRanking> rankings = cvRankingService.getBestCvsForJob(jobOfferId);

        if (rankings.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return ResponseEntity.ok(rankings);
    }

    @PostMapping("/job/{jobOfferId}/best/notify")
    public ResponseEntity<String> getBestCvsAndNotify(@PathVariable Long jobOfferId) {
        List<CvRanking> rankings = cvRankingService.getBestCvsForJob(jobOfferId);

        if (rankings.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        emailService.sendAcceptanceEmails(rankings, jobOfferId);

        return ResponseEntity.ok("Les " + rankings.size() + " meilleurs candidats ont été notifiés par email.");
    }

    @GetMapping("/job/{jobOfferId}/top/{topN}")
    public ResponseEntity<List<CvRanking>> getTopCvsForJob(
            @PathVariable Long jobOfferId,
            @PathVariable int topN) {

        if (topN <= 0 || topN > 20) {
            return ResponseEntity.badRequest().build();
        }

        List<CvRanking> rankings = cvRankingService.getTopCvsForJob(jobOfferId, topN);

        if (rankings.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return ResponseEntity.ok(rankings);
    }

    @PostMapping("/job/{jobOfferId}/top/{topN}/notify")
    public ResponseEntity<String> getTopCvsAndNotify(
            @PathVariable Long jobOfferId,
            @PathVariable int topN) {

        if (topN <= 0 || topN > 20) {
            return ResponseEntity.badRequest().build();
        }

        List<CvRanking> rankings = cvRankingService.getTopCvsForJob(jobOfferId, topN);

        if (rankings.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        emailService.sendAcceptanceEmails(rankings, jobOfferId);

        return ResponseEntity.ok("Les " + rankings.size() + " meilleurs candidats ont été notifiés par email.");
    }

    @GetMapping("/job/{jobOfferId}/details")
    public ResponseEntity<CvRankingDetails> getRankingDetails(@PathVariable Long jobOfferId) {
        List<CvRanking> rankings = cvRankingService.getBestCvsForJob(jobOfferId);

        if (rankings.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        CvRankingDetails details = new CvRankingDetails();
        details.setJobOfferId(jobOfferId);
        details.setTotalCvs(rankings.size());
        details.setRankings(rankings);

        double maxScore = rankings.stream().mapToDouble(CvRanking::getSimilarityScore).max().orElse(0.0);
        double minScore = rankings.stream().mapToDouble(CvRanking::getSimilarityScore).min().orElse(0.0);
        double avgScore = rankings.stream().mapToDouble(CvRanking::getSimilarityScore).average().orElse(0.0);

        details.setMaxSimilarityScore(maxScore);
        details.setMinSimilarityScore(minScore);
        details.setAverageSimilarityScore(avgScore);

        return ResponseEntity.ok(details);
    }


    public static class CvRankingDetails {
        private Long jobOfferId;
        private int totalCvs;
        private List<CvRanking> rankings;
        private double maxSimilarityScore;
        private double minSimilarityScore;
        private double averageSimilarityScore;

        public Long getJobOfferId() {
            return jobOfferId;
        }

        public void setJobOfferId(Long jobOfferId) {
            this.jobOfferId = jobOfferId;
        }

        public int getTotalCvs() {
            return totalCvs;
        }

        public void setTotalCvs(int totalCvs) {
            this.totalCvs = totalCvs;
        }

        public List<CvRanking> getRankings() {
            return rankings;
        }

        public void setRankings(List<CvRanking> rankings) {
            this.rankings = rankings;
        }

        public double getMaxSimilarityScore() {
            return maxSimilarityScore;
        }

        public void setMaxSimilarityScore(double maxSimilarityScore) {
            this.maxSimilarityScore = maxSimilarityScore;
        }

        public double getMinSimilarityScore() {
            return minSimilarityScore;
        }

        public void setMinSimilarityScore(double minSimilarityScore) {
            this.minSimilarityScore = minSimilarityScore;
        }

        public double getAverageSimilarityScore() {
            return averageSimilarityScore;
        }

        public void setAverageSimilarityScore(double averageSimilarityScore) {
            this.averageSimilarityScore = averageSimilarityScore;
        }
    }
}