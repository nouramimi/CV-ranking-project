package com.example.cvfilter.controller;

import com.example.cvfilter.dao.entity.CvScores;
import com.example.cvfilter.service.CvScoresService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/cv-scores")
@CrossOrigin(origins = "*")
public class CvScoresController {

    private static final Logger logger = LoggerFactory.getLogger(CvScoresController.class);

    @Autowired
    private CvScoresService cvScoresService;

    @GetMapping("/{userId}/{jobOfferId}")
    public ResponseEntity<CvScores> getCvScore(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        logger.info("Getting CV score for user={}, job={}", userId, jobOfferId);

        Optional<CvScores> cvScore = cvScoresService.getCvScore(userId, jobOfferId);

        if (cvScore.isPresent()) {
            return ResponseEntity.ok(cvScore.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/job/{jobOfferId}")
    public ResponseEntity<List<CvScores>> getScoresForJobOffer(@PathVariable Long jobOfferId) {
        logger.info("Getting all scores for job offer: {}", jobOfferId);

        List<CvScores> scores = cvScoresService.getScoresForJobOffer(jobOfferId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/job/{jobOfferId}/top")
    public ResponseEntity<List<CvScores>> getTopCvsForJob(
            @PathVariable Long jobOfferId,
            @RequestParam(defaultValue = "10") int limit) {

        logger.info("Getting top {} CVs for job offer: {}", limit, jobOfferId);

        List<CvScores> topCvs = cvScoresService.getTopCvsForJob(jobOfferId, limit);
        return ResponseEntity.ok(topCvs);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CvScores>> getScoresForUser(@PathVariable Long userId) {
        logger.info("Getting all scores for user: {}", userId);

        List<CvScores> scores = cvScoresService.getScoresForUser(userId);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/high-scoring")
    public ResponseEntity<List<CvScores>> getHighScoringCvs(
            @RequestParam(defaultValue = "80.0") double minScore) {

        logger.info("Getting CVs with organization score >= {}", minScore);

        List<CvScores> highScoringCvs = cvScoresService.getHighScoringCvs(minScore);
        return ResponseEntity.ok(highScoringCvs);
    }

    @GetMapping("/high-composite")
    public ResponseEntity<List<CvScores>> getHighCompositeScoringCvs(
            @RequestParam(defaultValue = "75.0") double minScore) {

        logger.info("Getting CVs with composite score >= {}", minScore);

        List<CvScores> highScoringCvs = cvScoresService.getHighCompositeScoringCvs(minScore);
        return ResponseEntity.ok(highScoringCvs);
    }

    @GetMapping("/job/{jobOfferId}/stats")
    public ResponseEntity<CvScoresService.JobOfferStats> getJobOfferStats(@PathVariable Long jobOfferId) {
        logger.info("Getting statistics for job offer: {}", jobOfferId);

        CvScoresService.JobOfferStats stats = cvScoresService.getJobOfferStats(jobOfferId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{userId}/{jobOfferId}/rank")
    public ResponseEntity<CvRankResponse> getCvRankForJob(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        logger.info("Getting rank for CV user={}, job={}", userId, jobOfferId);

        Long rank = cvScoresService.getCvRankForJob(userId, jobOfferId);

        if (rank != null) {
            return ResponseEntity.ok(new CvRankResponse(userId, jobOfferId, rank));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{userId}/{jobOfferId}/exists")
    public ResponseEntity<CvExistsResponse> checkCvScored(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        boolean exists = cvScoresService.isCvScored(userId, jobOfferId);
        return ResponseEntity.ok(new CvExistsResponse(userId, jobOfferId, exists));
    }

    @GetMapping("/summary")
    public ResponseEntity<CvScoresService.GlobalScoresSummary> getGlobalSummary() {
        logger.info("Getting global scores summary");

        CvScoresService.GlobalScoresSummary summary = cvScoresService.getGlobalSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/today-count")
    public ResponseEntity<Long> getCvsScoredToday() {
        Long count = cvScoresService.getCvsScoredToday();
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{userId}/{jobOfferId}")
    public ResponseEntity<DeleteResponse> deleteCvScore(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        logger.info("Deleting CV score for user={}, job={}", userId, jobOfferId);

        boolean deleted = cvScoresService.deleteCvScore(userId, jobOfferId);

        if (deleted) {
            return ResponseEntity.ok(new DeleteResponse(userId, jobOfferId, true, "Score deleted successfully"));
        } else {
            return ResponseEntity.ok(new DeleteResponse(userId, jobOfferId, false, "Failed to delete score"));
        }
    }

    @GetMapping("/unscored")
    public ResponseEntity<List<CvScores>> getUnscoredCvs() {
        logger.info("Getting unscored CVs");

        List<CvScores> unscoredCvs = cvScoresService.getUnscoredCvs();
        return ResponseEntity.ok(unscoredCvs);
    }

    public static class CvRankResponse {
        private final Long userId;
        private final Long jobOfferId;
        private final Long rank;

        public CvRankResponse(Long userId, Long jobOfferId, Long rank) {
            this.userId = userId;
            this.jobOfferId = jobOfferId;
            this.rank = rank;
        }

        public Long getUserId() { return userId; }
        public Long getJobOfferId() { return jobOfferId; }
        public Long getRank() { return rank; }
    }

    public static class CvExistsResponse {
        private final Long userId;
        private final Long jobOfferId;
        private final boolean exists;

        public CvExistsResponse(Long userId, Long jobOfferId, boolean exists) {
            this.userId = userId;
            this.jobOfferId = jobOfferId;
            this.exists = exists;
        }

        public Long getUserId() { return userId; }
        public Long getJobOfferId() { return jobOfferId; }
        public boolean isExists() { return exists; }
    }

    public static class DeleteResponse {
        private final Long userId;
        private final Long jobOfferId;
        private final boolean success;
        private final String message;

        public DeleteResponse(Long userId, Long jobOfferId, boolean success, String message) {
            this.userId = userId;
            this.jobOfferId = jobOfferId;
            this.success = success;
            this.message = message;
        }

        public Long getUserId() { return userId; }
        public Long getJobOfferId() { return jobOfferId; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}