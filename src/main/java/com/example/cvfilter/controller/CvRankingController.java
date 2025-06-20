package com.example.cvfilter.controller;

import com.example.cvfilter.config.JwtUtils;
import com.example.cvfilter.dao.entity.CvRanking;
import com.example.cvfilter.service.CvRankingService;
import com.example.cvfilter.service.EmailService;
import com.example.cvfilter.service.impl.CvRankingServiceInterface;
import com.example.cvfilter.service.impl.EmailServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/cv-ranking")
public class CvRankingController {

    private final CvRankingServiceInterface cvRankingService;
    private final EmailServiceInterface emailService;
    private final JwtUtils jwtUtils;

    public CvRankingController(CvRankingServiceInterface cvRankingService, EmailServiceInterface emailService, JwtUtils jwtUtils) {
        this.cvRankingService = cvRankingService;
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
    }

    // Contrôleur simplifié - logique dans le service
    @GetMapping("/best/{jobOfferId}")
    public ResponseEntity<List<CvRanking>> getBestCvsForJob(@PathVariable Long jobOfferId,
                                                            HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);
        List<CvRanking> rankings = cvRankingService.getBestCvsForJob(jobOfferId, username);
        return ResponseEntity.ok(rankings);
    }

    @GetMapping("/job/{jobOfferId}/top/{topN}")
    public ResponseEntity<List<CvRanking>> getTopCvsForJob(
            @PathVariable Long jobOfferId,
            @PathVariable int topN,
            HttpServletRequest request) {

        String username = extractUsernameFromRequest(request);
        List<CvRanking> rankings = cvRankingService.getTopCvsForJob(jobOfferId, topN, username);
        return ResponseEntity.ok(rankings);
    }

    @PostMapping("/job/{jobOfferId}/best/notify")
    public ResponseEntity<String> getBestCvsAndNotify(@PathVariable Long jobOfferId,
                                                      HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);
        int notifiedCount = cvRankingService.getBestCvsAndNotify(jobOfferId, username);
        return ResponseEntity.ok("Les " + notifiedCount + " meilleurs candidats ont été notifiés par email.");
    }

    @PostMapping("/job/{jobOfferId}/top/{topN}/notify")
    public ResponseEntity<String> getTopCvsAndNotify(
            @PathVariable Long jobOfferId,
            @PathVariable int topN,
            HttpServletRequest request) {

        String username = extractUsernameFromRequest(request);
        int notifiedCount = cvRankingService.getTopCvsAndNotify(jobOfferId, topN, username);
        return ResponseEntity.ok("Les " + notifiedCount + " meilleurs candidats ont été notifiés par email.");
    }

    @GetMapping("/job/{jobOfferId}/details")
    public ResponseEntity<CvRanking> getRankingDetails(@PathVariable Long jobOfferId,
                                                              HttpServletRequest request) {
        String username = extractUsernameFromRequest(request);
        CvRanking details = cvRankingService.getRankingDetails(jobOfferId, username);
        return ResponseEntity.ok(details);
    }
    private String extractUsernameFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        String username = jwtUtils.extractUsername(token);

        if (username == null) {
            throw new IllegalArgumentException("Unable to extract username from token");
        }

        return username;
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