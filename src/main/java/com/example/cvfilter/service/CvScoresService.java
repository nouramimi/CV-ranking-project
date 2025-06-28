package com.example.cvfilter.service;

import com.example.cvfilter.dao.entity.CvScores;
import com.example.cvfilter.dao.repository.CvScoresRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CvScoresService {

    private static final Logger logger = LoggerFactory.getLogger(CvScoresService.class);

    @Autowired
    private CvScoresRepository cvScoresRepository;

    public Optional<CvScores> getCvScore(Long userId, Long jobOfferId) {
        logger.debug("Getting CV score for user={}, job={}", userId, jobOfferId);
        return cvScoresRepository.findByUserIdAndJobOfferId(userId, jobOfferId);
    }

    public List<CvScores> getScoresForJobOffer(Long jobOfferId) {
        logger.info("Getting all scores for job offer: {}", jobOfferId);
        return cvScoresRepository.findByJobOfferIdOrderByOrganizationScoreDesc(jobOfferId);
    }

    public List<CvScores> getTopCvsForJob(Long jobOfferId, int limit) {
        logger.info("Getting top {} CVs for job offer: {}", limit, jobOfferId);
        List<CvScores> allScores = cvScoresRepository.findTopCvsForJob(jobOfferId);
        return allScores.stream().limit(limit).toList();
    }

    public List<CvScores> getHighScoringCvs(double minScore) {
        logger.info("Getting CVs with organization score >= {}", minScore);
        return cvScoresRepository.findByOrganizationScoreGreaterThanEqual(BigDecimal.valueOf(minScore));
    }

    public List<CvScores> getHighCompositeScoringCvs(double minScore) {
        logger.info("Getting CVs with composite score >= {}", minScore);
        return cvScoresRepository.findHighScoringCvs(BigDecimal.valueOf(minScore));
    }

    public List<CvScores> getScoresForUser(Long userId) {
        logger.info("Getting all scores for user: {}", userId);
        return cvScoresRepository.findByUserIdOrderByProcessedAtDesc(userId);
    }

    public JobOfferStats getJobOfferStats(Long jobOfferId) {
        logger.info("Getting statistics for job offer: {}", jobOfferId);

        Object[] stats = cvScoresRepository.getJobOfferStats(jobOfferId);
        if (stats != null && stats.length == 4) {
            return new JobOfferStats(
                    stats[0] != null ? ((Number) stats[0]).doubleValue() : 0.0, // avgOrgScore
                    stats[1] != null ? ((Number) stats[1]).doubleValue() : 0.0, // maxOrgScore
                    stats[2] != null ? ((Number) stats[2]).doubleValue() : 0.0, // minOrgScore
                    stats[3] != null ? ((Number) stats[3]).longValue() : 0L     // totalCvs
            );
        }

        return new JobOfferStats(0.0, 0.0, 0.0, 0L);
    }

    public Long getCvRankForJob(Long userId, Long jobOfferId) {
        Optional<CvScores> cvScore = getCvScore(userId, jobOfferId);
        if (cvScore.isPresent()) {
            CvScores score = cvScore.get();
            return cvScoresRepository.getCvRankForJob(
                    jobOfferId,
                    score.getOrganizationScore(),
                    score.getTechnicalScore()
            );
        }
        return null;
    }

    public boolean isCvScored(Long userId, Long jobOfferId) {
        return cvScoresRepository.existsByUserIdAndJobOfferId(userId, jobOfferId);
    }

    public Long getCvsScoredToday() {
        return cvScoresRepository.countCvsScoredToday();
    }

    public List<CvScores> getUnscoredCvs() {
        return cvScoresRepository.findUnscoredCvs();
    }

    @Transactional
    public boolean deleteCvScore(Long userId, Long jobOfferId) {
        logger.info("Deleting CV score for user={}, job={}", userId, jobOfferId);
        try {
            cvScoresRepository.deleteByUserIdAndJobOfferId(userId, jobOfferId);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting CV score for user={}, job={}", userId, jobOfferId, e);
            return false;
        }
    }

    public GlobalScoresSummary getGlobalSummary() {
        logger.info("Getting global scores summary");

        List<CvScores> allScores = cvScoresRepository.findAll();

        if (allScores.isEmpty()) {
            return new GlobalScoresSummary(0L, 0.0, 0.0, 0.0, 0L);
        }

        double avgOrgScore = allScores.stream()
                .filter(s -> s.getOrganizationScore() != null)
                .mapToDouble(s -> s.getOrganizationScore().doubleValue())
                .average()
                .orElse(0.0);

        double avgTechScore = allScores.stream()
                .filter(s -> s.getTechnicalScore() != null)
                .mapToDouble(s -> s.getTechnicalScore().doubleValue())
                .average()
                .orElse(0.0);

        double avgCompositeScore = allScores.stream()
                .filter(s -> s.getCompositeScore() != null)
                .mapToDouble(s -> s.getCompositeScore().doubleValue())
                .average()
                .orElse(0.0);

        long highScoringCount = allScores.stream()
                .filter(s -> s.getOrganizationScore() != null && s.getOrganizationScore().doubleValue() >= 80.0)
                .count();

        return new GlobalScoresSummary(
                (long) allScores.size(),
                avgOrgScore,
                avgTechScore,
                avgCompositeScore,
                highScoringCount
        );
    }

    public static class JobOfferStats {
        private final double avgOrganizationScore;
        private final double maxOrganizationScore;
        private final double minOrganizationScore;
        private final long totalCvs;

        public JobOfferStats(double avgOrganizationScore, double maxOrganizationScore,
                             double minOrganizationScore, long totalCvs) {
            this.avgOrganizationScore = avgOrganizationScore;
            this.maxOrganizationScore = maxOrganizationScore;
            this.minOrganizationScore = minOrganizationScore;
            this.totalCvs = totalCvs;
        }

        public double getAvgOrganizationScore() { return avgOrganizationScore; }
        public double getMaxOrganizationScore() { return maxOrganizationScore; }
        public double getMinOrganizationScore() { return minOrganizationScore; }
        public long getTotalCvs() { return totalCvs; }

        @Override
        public String toString() {
            return String.format("JobOfferStats{totalCvs=%d, avgScore=%.2f, maxScore=%.2f, minScore=%.2f}",
                    totalCvs, avgOrganizationScore, maxOrganizationScore, minOrganizationScore);
        }
    }

    public static class GlobalScoresSummary {
        private final long totalCvs;
        private final double avgOrganizationScore;
        private final double avgTechnicalScore;
        private final double avgCompositeScore;
        private final long highScoringCvs;

        public GlobalScoresSummary(long totalCvs, double avgOrganizationScore,
                                   double avgTechnicalScore, double avgCompositeScore,
                                   long highScoringCvs) {
            this.totalCvs = totalCvs;
            this.avgOrganizationScore = avgOrganizationScore;
            this.avgTechnicalScore = avgTechnicalScore;
            this.avgCompositeScore = avgCompositeScore;
            this.highScoringCvs = highScoringCvs;
        }

        public long getTotalCvs() { return totalCvs; }
        public double getAvgOrganizationScore() { return avgOrganizationScore; }
        public double getAvgTechnicalScore() { return avgTechnicalScore; }
        public double getAvgCompositeScore() { return avgCompositeScore; }
        public long getHighScoringCvs() { return highScoringCvs; }

        @Override
        public String toString() {
            return String.format("GlobalSummary{totalCvs=%d, avgOrgScore=%.2f, avgTechScore=%.2f, " +
                            "avgCompositeScore=%.2f, highScoringCvs=%d}",
                    totalCvs, avgOrganizationScore, avgTechnicalScore, avgCompositeScore, highScoringCvs);
        }
    }
}