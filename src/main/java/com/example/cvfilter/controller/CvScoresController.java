package com.example.cvfilter.controller;

import com.example.cvfilter.dao.entity.CvScores;
import com.example.cvfilter.dto.*;
import com.example.cvfilter.mapper.CvScoresMapper;
import com.example.cvfilter.service.CvScoresService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private CvScoresMapper cvScoresMapper;

    // === NOUVELLES MÉTHODES UTILISANT LES DTOs ===

    @GetMapping("/user/{userId}/job/{jobOfferId}")
    public ResponseEntity<CvScoresResponseDto> getCvScoreDto(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting CV score for user={}, job={} by user={}", userId, jobOfferId, userEmail);

        Optional<CvScores> cvScore = cvScoresService.getCvScore(userEmail, userId, jobOfferId);

        if (cvScore.isPresent()) {
            CvScoresResponseDto responseDto = cvScoresMapper.cvScoresToResponseDto(cvScore.get());
            return ResponseEntity.ok(responseDto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/job/{jobOfferId}")
    public ResponseEntity<PaginatedResponse<CvScoresResponseDto>> getScoresForJobOfferDto(
            @PathVariable Long jobOfferId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "finalScore") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting scores for job offer {} by user {} - page: {}, size: {}",
                jobOfferId, userEmail, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CvScores> scores = cvScoresService.getScoresForJobOffer(userEmail, jobOfferId, pageable);
        PaginatedResponse<CvScoresResponseDto> response = cvScoresMapper.toPageDto(scores);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/job/{jobOfferId}/top")
    public ResponseEntity<PaginatedResponse<CvScoresResponseDto>> getTopCvsForJobDto(
            @PathVariable Long jobOfferId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting top CVs for job {} by user {} - page: {}, size: {}",
                jobOfferId, userEmail, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("finalScore").descending());

        Page<CvScores> topCvs = cvScoresService.getTopCvsForJob(userEmail, jobOfferId, pageable);
        PaginatedResponse<CvScoresResponseDto> response = cvScoresMapper.toPageDto(topCvs);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PaginatedResponse<CvScoresResponseDto>> getScoresForUserDto(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "processedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting scores for user {} by user {} - page: {}, size: {}",
                userId, userEmail, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CvScores> scores = cvScoresService.getScoresForUser(userEmail, userId, pageable);
        PaginatedResponse<CvScoresResponseDto> response = cvScoresMapper.toPageDto(scores);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-scores")
    public ResponseEntity<PaginatedResponse<CvScoresResponseDto>> getMyScoresDto(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "processedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting own scores for user {} - page: {}, size: {}", userEmail, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Long currentUserId = cvScoresService.getCurrentUserId(userEmail);
        Page<CvScores> scores = cvScoresService.getScoresForUser(userEmail, currentUserId, pageable);
        PaginatedResponse<CvScoresResponseDto> response = cvScoresMapper.toPageDto(scores);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/high-scoring")
    public ResponseEntity<PaginatedResponse<CvScoresResponseDto>> getHighScoringCvsDto(
            @RequestParam(defaultValue = "80.0") double minScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting high scoring CVs (>= {}) by user {} - page: {}, size: {}",
                minScore, userEmail, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("finalScore").descending());

        Page<CvScores> highScoringCvs = cvScoresService.getHighScoringCvs(userEmail, minScore, pageable);
        PaginatedResponse<CvScoresResponseDto> response = cvScoresMapper.toPageDto(highScoringCvs);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/job/{jobOfferId}/stats")
    public ResponseEntity<JobOfferDetailedStatsDto> getJobOfferDetailedStatsDto(
            @PathVariable Long jobOfferId) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting detailed stats for job {} by user {}", jobOfferId, userEmail);

        CvScoresService.JobOfferDetailedStats stats = cvScoresService.getJobOfferDetailedStats(userEmail, jobOfferId);
        JobOfferDetailedStatsDto statsDto = cvScoresMapper.toJobOfferDetailedStatsDto(stats);

        return ResponseEntity.ok(statsDto);
    }

    @GetMapping("/user/{userId}/job/{jobOfferId}/rank")
    public ResponseEntity<CvRankResponseDto> getCvRankForJobDto(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting rank for CV user={}, job={} by user={}", userId, jobOfferId, userEmail);

        Long rank = cvScoresService.getCvRankForJob(userEmail, userId, jobOfferId);

        if (rank != null) {
            CvRankResponseDto response = cvScoresMapper.createCvRankResponseDto(userId, jobOfferId, rank);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my-rank/job/{jobOfferId}")
    public ResponseEntity<CvRankResponseDto> getMyRankForJobDto(@PathVariable Long jobOfferId) {
        String userEmail = getCurrentUserEmail();
        Long currentUserId = cvScoresService.getCurrentUserId(userEmail);

        logger.info("Getting own rank for job {} by user {}", jobOfferId, userEmail);

        Long rank = cvScoresService.getCvRankForJob(userEmail, currentUserId, jobOfferId);

        if (rank != null) {
            CvRankResponseDto response = cvScoresMapper.createCvRankResponseDto(currentUserId, jobOfferId, rank);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}/job/{jobOfferId}/exists")
    public ResponseEntity<CvExistsResponseDto> checkCvScoredDto(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        String userEmail = getCurrentUserEmail();

        // Vérifier l'autorisation d'accès à cette information
        try {
            cvScoresService.getCvScore(userEmail, userId, jobOfferId);
            boolean exists = cvScoresService.isCvScored(userId, jobOfferId);
            CvExistsResponseDto response = cvScoresMapper.createCvExistsResponseDto(userId, jobOfferId, exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CvExistsResponseDto response = cvScoresMapper.createCvExistsResponseDto(userId, jobOfferId, false);
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<GlobalScoresSummaryDto> getGlobalSummaryDto() {
        String userEmail = getCurrentUserEmail();
        logger.info("Getting global summary by user {}", userEmail);

        CvScoresService.GlobalScoresSummary summary = cvScoresService.getGlobalSummary(userEmail);
        GlobalScoresSummaryDto summaryDto = cvScoresMapper.toGlobalScoresSummaryDto(summary);

        return ResponseEntity.ok(summaryDto);
    }

    @DeleteMapping("/user/{userId}/job/{jobOfferId}")
    public ResponseEntity<DeleteResponseDto> deleteCvScoreDto(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        String userEmail = getCurrentUserEmail();
        logger.info("Deleting CV score for user={}, job={} by user={}", userId, jobOfferId, userEmail);

        boolean deleted = cvScoresService.deleteCvScore(userEmail, userId, jobOfferId);

        DeleteResponseDto response;
        if (deleted) {
            response = cvScoresMapper.createDeleteResponseDto(userId, jobOfferId, true, "Score deleted successfully");
        } else {
            response = cvScoresMapper.createDeleteResponseDto(userId, jobOfferId, false, "Failed to delete score");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/match-level/{level}")
    public ResponseEntity<PaginatedResponse<CvScoresResponseDto>> getCvsByMatchLevelDto(
            @PathVariable String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting CVs with match level {} by user {} - page: {}, size: {}",
                level, userEmail, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("finalScore").descending());

        Page<CvScores> cvs = cvScoresService.getCvsByMatchLevel(userEmail, level.toUpperCase(), pageable);
        PaginatedResponse<CvScoresResponseDto> response = cvScoresMapper.toPageDto(cvs);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/job/{jobOfferId}/compare")
    public ResponseEntity<CvComparisonResponseDto> compareCvsDto(
            @PathVariable Long jobOfferId,
            @RequestParam List<Long> userIds) {

        String userEmail = getCurrentUserEmail();
        logger.info("Comparing {} CVs for job {} by user {}", userIds.size(), jobOfferId, userEmail);

        CvScoresService.CvComparisonResponse comparison = cvScoresService.compareCvs(userEmail, jobOfferId, userIds);
        CvComparisonResponseDto comparisonDto = cvScoresMapper.toCvComparisonResponseDto(comparison);

        return ResponseEntity.ok(comparisonDto);
    }

    // === MÉTHODES HÉRITÉES POUR COMPATIBILITÉ ===

    @GetMapping("/legacy/user/{userId}/job/{jobOfferId}")
    public ResponseEntity<CvScores> getCvScore(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting CV score for user={}, job={} by user={}", userId, jobOfferId, userEmail);

        Optional<CvScores> cvScore = cvScoresService.getCvScore(userEmail, userId, jobOfferId);

        if (cvScore.isPresent()) {
            return ResponseEntity.ok(cvScore.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/legacy/job/{jobOfferId}")
    public ResponseEntity<Page<CvScores>> getScoresForJobOffer(
            @PathVariable Long jobOfferId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "finalScore") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting scores for job offer {} by user {} - page: {}, size: {}",
                jobOfferId, userEmail, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CvScores> scores = cvScoresService.getScoresForJobOffer(userEmail, jobOfferId, pageable);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/legacy/job/{jobOfferId}/top")
    public ResponseEntity<Page<CvScores>> getTopCvsForJob(
            @PathVariable Long jobOfferId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting top CVs for job {} by user {} - page: {}, size: {}",
                jobOfferId, userEmail, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("finalScore").descending());

        Page<CvScores> topCvs = cvScoresService.getTopCvsForJob(userEmail, jobOfferId, pageable);
        return ResponseEntity.ok(topCvs);
    }

    @GetMapping("/legacy/user/{userId}")
    public ResponseEntity<Page<CvScores>> getScoresForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "processedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting scores for user {} by user {} - page: {}, size: {}",
                userId, userEmail, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CvScores> scores = cvScoresService.getScoresForUser(userEmail, userId, pageable);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/legacy/my-scores")
    public ResponseEntity<Page<CvScores>> getMyScores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "processedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting own scores for user {} - page: {}, size: {}", userEmail, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Long currentUserId = cvScoresService.getCurrentUserId(userEmail);
        Page<CvScores> scores = cvScoresService.getScoresForUser(userEmail, currentUserId, pageable);
        return ResponseEntity.ok(scores);
    }

    @GetMapping("/legacy/high-scoring")
    public ResponseEntity<Page<CvScores>> getHighScoringCvs(
            @RequestParam(defaultValue = "80.0") double minScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting high scoring CVs (>= {}) by user {} - page: {}, size: {}",
                minScore, userEmail, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("finalScore").descending());

        Page<CvScores> highScoringCvs = cvScoresService.getHighScoringCvs(userEmail, minScore, pageable);
        return ResponseEntity.ok(highScoringCvs);
    }

    @GetMapping("/legacy/job/{jobOfferId}/stats")
    public ResponseEntity<CvScoresService.JobOfferDetailedStats> getJobOfferDetailedStats(
            @PathVariable Long jobOfferId) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting detailed stats for job {} by user {}", jobOfferId, userEmail);

        CvScoresService.JobOfferDetailedStats stats = cvScoresService.getJobOfferDetailedStats(userEmail, jobOfferId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/legacy/user/{userId}/job/{jobOfferId}/rank")
    public ResponseEntity<CvRankResponse> getCvRankForJob(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting rank for CV user={}, job={} by user={}", userId, jobOfferId, userEmail);

        Long rank = cvScoresService.getCvRankForJob(userEmail, userId, jobOfferId);

        if (rank != null) {
            return ResponseEntity.ok(new CvRankResponse(userId, jobOfferId, rank));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/legacy/my-rank/job/{jobOfferId}")
    public ResponseEntity<CvRankResponse> getMyRankForJob(@PathVariable Long jobOfferId) {
        String userEmail = getCurrentUserEmail();
        Long currentUserId = cvScoresService.getCurrentUserId(userEmail);

        logger.info("Getting own rank for job {} by user {}", jobOfferId, userEmail);

        Long rank = cvScoresService.getCvRankForJob(userEmail, currentUserId, jobOfferId);

        if (rank != null) {
            return ResponseEntity.ok(new CvRankResponse(currentUserId, jobOfferId, rank));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/legacy/user/{userId}/job/{jobOfferId}/exists")
    public ResponseEntity<CvExistsResponse> checkCvScored(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        String userEmail = getCurrentUserEmail();

        try {
            cvScoresService.getCvScore(userEmail, userId, jobOfferId);
            boolean exists = cvScoresService.isCvScored(userId, jobOfferId);
            return ResponseEntity.ok(new CvExistsResponse(userId, jobOfferId, exists));
        } catch (Exception e) {
            return ResponseEntity.ok(new CvExistsResponse(userId, jobOfferId, false));
        }
    }

    @GetMapping("/legacy/summary")
    public ResponseEntity<CvScoresService.GlobalScoresSummary> getGlobalSummary() {
        String userEmail = getCurrentUserEmail();
        logger.info("Getting global summary by user {}", userEmail);

        CvScoresService.GlobalScoresSummary summary = cvScoresService.getGlobalSummary(userEmail);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/today-count")
    public ResponseEntity<Long> getCvsScoredToday() {
        String userEmail = getCurrentUserEmail();

        Long count = cvScoresService.getCvsScoredToday();
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/legacy/user/{userId}/job/{jobOfferId}")
    public ResponseEntity<DeleteResponse> deleteCvScore(
            @PathVariable Long userId,
            @PathVariable Long jobOfferId) {

        String userEmail = getCurrentUserEmail();
        logger.info("Deleting CV score for user={}, job={} by user={}", userId, jobOfferId, userEmail);

        boolean deleted = cvScoresService.deleteCvScore(userEmail, userId, jobOfferId);

        if (deleted) {
            return ResponseEntity.ok(new DeleteResponse(userId, jobOfferId, true, "Score deleted successfully"));
        } else {
            return ResponseEntity.ok(new DeleteResponse(userId, jobOfferId, false, "Failed to delete score"));
        }
    }

    @GetMapping("/legacy/match-level/{level}")
    public ResponseEntity<Page<CvScores>> getCvsByMatchLevel(
            @PathVariable String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String userEmail = getCurrentUserEmail();
        logger.info("Getting CVs with match level {} by user {} - page: {}, size: {}",
                level, userEmail, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("finalScore").descending());

        Page<CvScores> cvs = cvScoresService.getCvsByMatchLevel(userEmail, level.toUpperCase(), pageable);
        return ResponseEntity.ok(cvs);
    }

    @GetMapping("/legacy/job/{jobOfferId}/compare")
    public ResponseEntity<CvScoresService.CvComparisonResponse> compareCvs(
            @PathVariable Long jobOfferId,
            @RequestParam List<Long> userIds) {

        String userEmail = getCurrentUserEmail();
        logger.info("Comparing {} CVs for job {} by user {}", userIds.size(), jobOfferId, userEmail);

        CvScoresService.CvComparisonResponse comparison = cvScoresService.compareCvs(userEmail, jobOfferId, userIds);
        return ResponseEntity.ok(comparison);
    }

    // === MÉTHODE UTILITAIRE ===

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName(); // Supposé être l'email
        }
        throw new RuntimeException("No authenticated user found");
    }

    // === CLASSES INTERNES LEGACY ===

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