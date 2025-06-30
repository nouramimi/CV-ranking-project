package com.example.cvfilter.service;

import com.example.cvfilter.dao.UserDao;
import com.example.cvfilter.dao.entity.CvScores;
import com.example.cvfilter.dao.entity.Role;
import com.example.cvfilter.dao.entity.User;
import com.example.cvfilter.dao.entity.HRManager;
import com.example.cvfilter.dao.repository.CvScoresRepository;
import com.example.cvfilter.dao.repository.JobOfferRepository;
import com.example.cvfilter.dto.*;
import com.example.cvfilter.exception.UnauthorizedAccessException;
import com.example.cvfilter.mapper.CvScoresMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CvScoresService {

    private static final Logger logger = LoggerFactory.getLogger(CvScoresService.class);

    @Autowired
    private CvScoresRepository cvScoresRepository;

    @Autowired
    private UserDao userDao;

    @Autowired
    private JobOfferRepository jobOfferRepository;

    @Autowired
    private CvScoresMapper cvScoresMapper;

    // === NOUVELLES MÉTHODES UTILISANT LES DTOs ===

    public Optional<CvScoresResponseDto> getCvScoreDto(String userEmail, Long targetUserId, Long jobOfferId) {
        logger.debug("Getting CV score DTO for target user={}, job={} by user={}", targetUserId, jobOfferId, userEmail);

        Optional<CvScores> cvScore = getCvScore(userEmail, targetUserId, jobOfferId);
        return cvScore.map(cvScoresMapper::cvScoresToResponseDto);
    }

    public PaginatedResponse<CvScoresResponseDto> getScoresForJobOfferDto(String userEmail, Long jobOfferId, Pageable pageable) {
        logger.info("Getting scores DTO for job offer {} by user {} with pagination", jobOfferId, userEmail);

        Page<CvScores> scores = getScoresForJobOffer(userEmail, jobOfferId, pageable);
        return cvScoresMapper.toPageDto(scores);
    }

    public PaginatedResponse<CvScoresResponseDto> getTopCvsForJobDto(String userEmail, Long jobOfferId, Pageable pageable) {
        logger.info("Getting top CVs DTO for job {} by user {} with pagination", jobOfferId, userEmail);

        Page<CvScores> topCvs = getTopCvsForJob(userEmail, jobOfferId, pageable);
        return cvScoresMapper.toPageDto(topCvs);
    }

    public PaginatedResponse<CvScoresResponseDto> getScoresForUserDto(String userEmail, Long targetUserId, Pageable pageable) {
        logger.info("Getting scores DTO for user {} by user {} with pagination", targetUserId, userEmail);

        Page<CvScores> scores = getScoresForUser(userEmail, targetUserId, pageable);
        return cvScoresMapper.toPageDto(scores);
    }

    public PaginatedResponse<CvScoresResponseDto> getHighScoringCvsDto(String userEmail, double minScore, Pageable pageable) {
        logger.info("Getting high scoring CVs DTO (>= {}) by user {} with pagination", minScore, userEmail);

        Page<CvScores> highScoringCvs = getHighScoringCvs(userEmail, minScore, pageable);
        return cvScoresMapper.toPageDto(highScoringCvs);
    }

    public JobOfferDetailedStatsDto getJobOfferDetailedStatsDto(String userEmail, Long jobOfferId) {
        logger.info("Getting detailed stats DTO for job {} by user {}", jobOfferId, userEmail);

        JobOfferDetailedStats stats = getJobOfferDetailedStats(userEmail, jobOfferId);
        return cvScoresMapper.toJobOfferDetailedStatsDto(stats);
    }

    public CvRankResponseDto getCvRankForJobDto(String userEmail, Long targetUserId, Long jobOfferId) {
        logger.info("Getting rank DTO for CV user={}, job={} by user={}", targetUserId, jobOfferId, userEmail);

        Long rank = getCvRankForJob(userEmail, targetUserId, jobOfferId);
        if (rank != null) {
            return cvScoresMapper.createCvRankResponseDto(targetUserId, jobOfferId, rank);
        }
        return null;
    }

    public CvExistsResponseDto checkCvScoredDto(String userEmail, Long userId, Long jobOfferId) {
        try {
            getCvScore(userEmail, userId, jobOfferId);
            boolean exists = isCvScored(userId, jobOfferId);
            return cvScoresMapper.createCvExistsResponseDto(userId, jobOfferId, exists);
        } catch (Exception e) {
            return cvScoresMapper.createCvExistsResponseDto(userId, jobOfferId, false);
        }
    }

    public GlobalScoresSummaryDto getGlobalSummaryDto(String userEmail) {
        logger.info("Getting global summary DTO by user {}", userEmail);

        GlobalScoresSummary summary = getGlobalSummary(userEmail);
        return cvScoresMapper.toGlobalScoresSummaryDto(summary);
    }

    public DeleteResponseDto deleteCvScoreDto(String userEmail, Long targetUserId, Long jobOfferId) {
        logger.info("Deleting CV score DTO for user={}, job={} by user={}", targetUserId, jobOfferId, userEmail);

        boolean deleted = deleteCvScore(userEmail, targetUserId, jobOfferId);
        String message = deleted ? "Score deleted successfully" : "Failed to delete score";
        return cvScoresMapper.createDeleteResponseDto(targetUserId, jobOfferId, deleted, message);
    }

    public PaginatedResponse<CvScoresResponseDto> getCvsByMatchLevelDto(String userEmail, String matchLevel, Pageable pageable) {
        logger.info("Getting CVs DTO with match level {} by user {} with pagination", matchLevel, userEmail);

        Page<CvScores> cvs = getCvsByMatchLevel(userEmail, matchLevel, pageable);
        return cvScoresMapper.toPageDto(cvs);
    }

    public CvComparisonResponseDto compareCvsDto(String userEmail, Long jobOfferId, List<Long> userIds) {
        logger.info("Comparing {} CVs DTO for job {} by user {}", userIds.size(), jobOfferId, userEmail);

        CvComparisonResponse comparison = compareCvs(userEmail, jobOfferId, userIds);
        return cvScoresMapper.toCvComparisonResponseDto(comparison);
    }

    // === MÉTHODES HÉRITÉES (LOGIC MÉTIER CORE) ===

    public Optional<CvScores> getCvScore(String userEmail, Long targetUserId, Long jobOfferId) {
        logger.debug("Getting CV score for target user={}, job={} by user={}", targetUserId, jobOfferId, userEmail);

        User currentUser = getCurrentUser(userEmail);

        if (!canAccessCvScore(currentUser, targetUserId, jobOfferId)) {
            throw new UnauthorizedAccessException("Access denied to CV score");
        }

        return cvScoresRepository.findByUserIdAndJobOfferId(targetUserId, jobOfferId);
    }

    public Page<CvScores> getScoresForJobOffer(String userEmail, Long jobOfferId, Pageable pageable) {
        logger.info("Getting scores for job offer {} by user {} with pagination", jobOfferId, userEmail);

        User currentUser = getCurrentUser(userEmail);

        switch (currentUser.getRole()) {
            case ADMIN:
                return cvScoresRepository.findByJobOfferIdOrderByFinalScoreDesc(jobOfferId, pageable);

            case HR_MANAGER:
                HRManager hrManager = (HRManager) currentUser;
                if (!isJobFromHRManagerCompany(jobOfferId, hrManager.getCompany().getId())) {
                    throw new UnauthorizedAccessException("HR Manager can only access jobs from their company");
                }
                return cvScoresRepository.findByJobOfferIdOrderByFinalScoreDesc(jobOfferId, pageable);

            case USER:
                return cvScoresRepository.findByUserIdAndJobOfferIdOrderByProcessedAtDesc(
                        currentUser.getId(), jobOfferId, pageable);

            default:
                throw new UnauthorizedAccessException("Invalid role");
        }
    }

    public Page<CvScores> getTopCvsForJob(String userEmail, Long jobOfferId, Pageable pageable) {
        logger.info("Getting top CVs for job {} by user {} with pagination", jobOfferId, userEmail);

        User currentUser = getCurrentUser(userEmail);

        if (currentUser.getRole() == Role.USER) {
            return cvScoresRepository.findByUserIdAndJobOfferIdOrderByProcessedAtDesc(
                    currentUser.getId(), jobOfferId, pageable);
        }

        if (currentUser.getRole() == Role.HR_MANAGER) {
            HRManager hrManager = (HRManager) currentUser;
            if (!isJobFromHRManagerCompany(jobOfferId, hrManager.getCompany().getId())) {
                throw new UnauthorizedAccessException("HR Manager can only access jobs from their company");
            }
        }

        return cvScoresRepository.findTopCvsForJobByFinalScore(jobOfferId, pageable);
    }

    public Page<CvScores> getHighScoringCvs(String userEmail, double minScore, Pageable pageable) {
        User currentUser = getCurrentUser(userEmail);

        switch (currentUser.getRole()) {
            case ADMIN:
                return cvScoresRepository.findByFinalScoreGreaterThanEqual(BigDecimal.valueOf(minScore), pageable);

            case HR_MANAGER:
                HRManager hrManager = (HRManager) currentUser;
                return cvScoresRepository.findByFinalScoreGreaterThanEqualAndCompany(
                        BigDecimal.valueOf(minScore), hrManager.getCompany().getId(), pageable);

            case USER:
                return cvScoresRepository.findByUserIdAndFinalScoreGreaterThanEqual(
                        currentUser.getId(), BigDecimal.valueOf(minScore), pageable);

            default:
                throw new UnauthorizedAccessException("Invalid role");
        }
    }

    public Page<CvScores> getScoresForUser(String userEmail, Long targetUserId, Pageable pageable) {
        User currentUser = getCurrentUser(userEmail);

        if (currentUser.getRole() == Role.USER && !currentUser.getId().equals(targetUserId)) {
            throw new UnauthorizedAccessException("Users can only access their own scores");
        }

        if (currentUser.getRole() == Role.HR_MANAGER) {
            HRManager hrManager = (HRManager) currentUser;
            if (!isUserFromHRManagerCompany(targetUserId, hrManager.getCompany().getId())) {
                throw new UnauthorizedAccessException("HR Manager can only access users from their company");
            }
        }

        return cvScoresRepository.findByUserIdOrderByProcessedAtDesc(targetUserId, pageable);
    }

    public JobOfferDetailedStats getJobOfferDetailedStats(String userEmail, Long jobOfferId) {
        User currentUser = getCurrentUser(userEmail);

        if (currentUser.getRole() == Role.USER) {
            throw new UnauthorizedAccessException("Users cannot access job offer statistics");
        }

        if (currentUser.getRole() == Role.HR_MANAGER) {
            HRManager hrManager = (HRManager) currentUser;
            if (!isJobFromHRManagerCompany(jobOfferId, hrManager.getCompany().getId())) {
                throw new UnauthorizedAccessException("HR Manager can only access jobs from their company");
            }
        }

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<CvScores> scoresPage = getScoresForJobOffer(userEmail, jobOfferId, pageable);
        List<CvScores> scores = scoresPage.getContent();

        if (scores.isEmpty()) {
            return new JobOfferDetailedStats(0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    new MatchLevelDistribution(0, 0, 0, 0));
        }

        double avgFinalScore = scores.stream()
                .filter(s -> s.getFinalScore() != null)
                .mapToDouble(s -> s.getFinalScore().doubleValue())
                .average().orElse(0.0);

        double avgJobMatchScore = scores.stream()
                .filter(s -> s.getJobMatchScore() != null)
                .mapToDouble(s -> s.getJobMatchScore().doubleValue())
                .average().orElse(0.0);

        double avgOrgScore = scores.stream()
                .filter(s -> s.getOrganizationScore() != null)
                .mapToDouble(s -> s.getOrganizationScore().doubleValue())
                .average().orElse(0.0);

        double maxFinalScore = scores.stream()
                .filter(s -> s.getFinalScore() != null)
                .mapToDouble(s -> s.getFinalScore().doubleValue())
                .max().orElse(0.0);

        double minFinalScore = scores.stream()
                .filter(s -> s.getFinalScore() != null)
                .mapToDouble(s -> s.getFinalScore().doubleValue())
                .min().orElse(0.0);

        long excellent = scores.stream().filter(s -> "EXCELLENT".equals(s.getMatchLevel())).count();
        long good = scores.stream().filter(s -> "GOOD".equals(s.getMatchLevel())).count();
        long fair = scores.stream().filter(s -> "FAIR".equals(s.getMatchLevel())).count();
        long poor = scores.stream().filter(s -> "POOR".equals(s.getMatchLevel())).count();

        return new JobOfferDetailedStats(
                scores.size(), avgFinalScore, avgJobMatchScore, avgOrgScore, maxFinalScore, minFinalScore,
                new MatchLevelDistribution(excellent, good, fair, poor)
        );
    }

    public Long getCvRankForJob(String userEmail, Long targetUserId, Long jobOfferId) {
        User currentUser = getCurrentUser(userEmail);

        if (!canAccessCvScore(currentUser, targetUserId, jobOfferId)) {
            throw new UnauthorizedAccessException("Access denied to CV rank");
        }

        Optional<CvScores> cvScore = cvScoresRepository.findByUserIdAndJobOfferId(targetUserId, jobOfferId);
        if (cvScore.isPresent()) {
            CvScores score = cvScore.get();
            return cvScoresRepository.getCvRankForJobByFinalScore(
                    jobOfferId, score.getFinalScore());
        }
        return null;
    }

    public boolean isCvScored(Long userId, Long jobOfferId) {
        return cvScoresRepository.existsByUserIdAndJobOfferId(userId, jobOfferId);
    }

    public Long getCvsScoredToday() {
        return cvScoresRepository.countCvsScoredToday();
    }

    @Transactional
    public boolean deleteCvScore(String userEmail, Long targetUserId, Long jobOfferId) {
        User currentUser = getCurrentUser(userEmail);

        if (currentUser.getRole() == Role.USER) {
            throw new UnauthorizedAccessException("Users cannot delete CV scores");
        }

        if (currentUser.getRole() == Role.HR_MANAGER) {
            HRManager hrManager = (HRManager) currentUser;
            if (!isJobFromHRManagerCompany(jobOfferId, hrManager.getCompany().getId()) ||
                    !isUserFromHRManagerCompany(targetUserId, hrManager.getCompany().getId())) {
                throw new UnauthorizedAccessException("HR Manager can only delete scores from their company");
            }
        }

        logger.info("Deleting CV score for user={}, job={} by {}", targetUserId, jobOfferId, userEmail);
        try {
            cvScoresRepository.deleteByUserIdAndJobOfferId(targetUserId, jobOfferId);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting CV score for user={}, job={}", targetUserId, jobOfferId, e);
            return false;
        }
    }

    public GlobalScoresSummary getGlobalSummary(String userEmail) {
        User currentUser = getCurrentUser(userEmail);

        List<CvScores> allScores;
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);

        switch (currentUser.getRole()) {
            case ADMIN:
                allScores = cvScoresRepository.findAll(pageable).getContent();
                break;

            case HR_MANAGER:
                HRManager hrManager = (HRManager) currentUser;
                allScores = cvScoresRepository.findAllByCompany(hrManager.getCompany().getId(), pageable).getContent();
                break;

            case USER:
                allScores = cvScoresRepository.findByUserIdOrderByProcessedAtDesc(currentUser.getId(), pageable).getContent();
                break;

            default:
                throw new UnauthorizedAccessException("Invalid role");
        }

        if (allScores.isEmpty()) {
            return new GlobalScoresSummary(0L, 0.0, 0.0, 0.0, 0L, 0L,
                    new MatchLevelDistribution(0, 0, 0, 0));
        }

        double avgOrgScore = allScores.stream()
                .filter(s -> s.getOrganizationScore() != null)
                .mapToDouble(s -> s.getOrganizationScore().doubleValue())
                .average().orElse(0.0);

        double avgJobMatchScore = allScores.stream()
                .filter(s -> s.getJobMatchScore() != null)
                .mapToDouble(s -> s.getJobMatchScore().doubleValue())
                .average().orElse(0.0);

        double avgFinalScore = allScores.stream()
                .filter(s -> s.getFinalScore() != null)
                .mapToDouble(s -> s.getFinalScore().doubleValue())
                .average().orElse(0.0);

        long excellentCount = allScores.stream()
                .filter(s -> "EXCELLENT".equals(s.getMatchLevel())).count();

        long goodCount = allScores.stream()
                .filter(s -> "GOOD".equals(s.getMatchLevel())).count();

        long fair = allScores.stream().filter(s -> "FAIR".equals(s.getMatchLevel())).count();
        long poor = allScores.stream().filter(s -> "POOR".equals(s.getMatchLevel())).count();

        return new GlobalScoresSummary(
                (long) allScores.size(), avgFinalScore, avgJobMatchScore, avgOrgScore,
                excellentCount, goodCount, new MatchLevelDistribution(excellentCount, goodCount, fair, poor)
        );
    }

    public Long getCurrentUserId(String userEmail) {
        User user = getCurrentUser(userEmail);
        return user.getId();
    }

    public Page<CvScores> getCvsByMatchLevel(String userEmail, String matchLevel, Pageable pageable) {
        User currentUser = getCurrentUser(userEmail);

        switch (currentUser.getRole()) {
            case ADMIN:
                return cvScoresRepository.findByMatchLevelOrderByFinalScoreDesc(matchLevel, pageable);

            case HR_MANAGER:
                HRManager hrManager = (HRManager) currentUser;
                return cvScoresRepository.findByMatchLevelAndCompanyOrderByFinalScoreDesc(
                        matchLevel, hrManager.getCompany().getId(), pageable);

            case USER:
                return cvScoresRepository.findByUserIdAndMatchLevelOrderByFinalScoreDesc(
                        currentUser.getId(), matchLevel, pageable);

            default:
                throw new UnauthorizedAccessException("Invalid role");
        }
    }

    public CvComparisonResponse compareCvs(String userEmail, Long jobOfferId, List<Long> userIds) {
        User currentUser = getCurrentUser(userEmail);

        if (currentUser.getRole() == Role.USER) {
            throw new UnauthorizedAccessException("Users cannot compare CVs");
        }

        if (currentUser.getRole() == Role.HR_MANAGER) {
            HRManager hrManager = (HRManager) currentUser;
            if (!isJobFromHRManagerCompany(jobOfferId, hrManager.getCompany().getId())) {
                throw new UnauthorizedAccessException("HR Manager can only compare CVs from their company jobs");
            }

            for (Long userId : userIds) {
                if (!isUserFromHRManagerCompany(userId, hrManager.getCompany().getId())) {
                    throw new UnauthorizedAccessException("HR Manager can only compare CVs from their company");
                }
            }
        }

        List<CvComparisonItem> comparisonItems = new ArrayList<>();
        CvComparisonItem winner = null;
        Double maxScore = 0.0;

        for (Long userId : userIds) {
            Optional<CvScores> cvScore = cvScoresRepository.findByUserIdAndJobOfferId(userId, jobOfferId);

            if (cvScore.isPresent()) {
                CvScores score = cvScore.get();
                Double finalScore = score.getFinalScore() != null ? score.getFinalScore().doubleValue() : 0.0;
                Double jobMatchScore = score.getJobMatchScore() != null ? score.getJobMatchScore().doubleValue() : 0.0;
                Double orgScore = score.getOrganizationScore() != null ? score.getOrganizationScore().doubleValue() : 0.0;

                Long rank = cvScoresRepository.getCvRankForJobByFinalScore(jobOfferId, score.getFinalScore());

                CvComparisonItem item = new CvComparisonItem(
                        userId, finalScore, jobMatchScore, orgScore, score.getMatchLevel(), rank
                );

                comparisonItems.add(item);

                if (finalScore > maxScore) {
                    maxScore = finalScore;
                    winner = item;
                }
            }
        }

        comparisonItems.sort((a, b) -> Double.compare(b.getFinalScore(), a.getFinalScore()));

        return new CvComparisonResponse(jobOfferId, comparisonItems, winner);
    }

    // === MÉTHODES UTILITAIRES PRIVÉES ===

    private boolean canAccessCvScore(User currentUser, Long targetUserId, Long jobOfferId) {
        switch (currentUser.getRole()) {
            case ADMIN:
                return true;

            case HR_MANAGER:
                HRManager hrManager = (HRManager) currentUser;
                return isJobFromHRManagerCompany(jobOfferId, hrManager.getCompany().getId()) &&
                        isUserFromHRManagerCompany(targetUserId, hrManager.getCompany().getId());

            case USER:
                return currentUser.getId().equals(targetUserId);

            default:
                return false;
        }
    }

    private boolean isJobFromHRManagerCompany(Long jobOfferId, Long companyId) {
        return jobOfferRepository.existsByIdAndCompanyId(jobOfferId, companyId);
    }

    private boolean isUserFromHRManagerCompany(Long userId, Long companyId) {
        return cvScoresRepository.existsUserInCompany(userId, companyId);
    }

    private User getCurrentUser(String userEmail) {
        return userDao.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedAccessException("User not found: " + userEmail));
    }

    // === CLASSES INTERNES CONSERVÉES POUR COMPATIBILITÉ ===

    public static class CvComparisonResponse {
        private final Long jobOfferId;
        private final List<CvComparisonItem> cvs;
        private final CvComparisonItem winner;

        public CvComparisonResponse(Long jobOfferId, List<CvComparisonItem> cvs, CvComparisonItem winner) {
            this.jobOfferId = jobOfferId;
            this.cvs = cvs;
            this.winner = winner;
        }

        public Long getJobOfferId() { return jobOfferId; }
        public List<CvComparisonItem> getCvs() { return cvs; }
        public CvComparisonItem getWinner() { return winner; }
    }

    public static class CvComparisonItem {
        private final Long userId;
        private final Double finalScore;
        private final Double jobMatchScore;
        private final Double organizationScore;
        private final String matchLevel;
        private final Long rank;

        public CvComparisonItem(Long userId, Double finalScore, Double jobMatchScore,
                                Double organizationScore, String matchLevel, Long rank) {
            this.userId = userId;
            this.finalScore = finalScore;
            this.jobMatchScore = jobMatchScore;
            this.organizationScore = organizationScore;
            this.matchLevel = matchLevel;
            this.rank = rank;
        }

        public Long getUserId() { return userId; }
        public Double getFinalScore() { return finalScore; }
        public Double getJobMatchScore() { return jobMatchScore; }
        public Double getOrganizationScore() { return organizationScore; }
        public String getMatchLevel() { return matchLevel; }
        public Long getRank() { return rank; }
    }

    public static class JobOfferDetailedStats {
        private final long totalCvs;
        private final double avgFinalScore;
        private final double avgJobMatchScore;
        private final double avgOrganizationScore;
        private final double maxFinalScore;
        private final double minFinalScore;
        private final MatchLevelDistribution matchLevelDistribution;

        public JobOfferDetailedStats(long totalCvs, double avgFinalScore, double avgJobMatchScore,
                                     double avgOrganizationScore, double maxFinalScore, double minFinalScore,
                                     MatchLevelDistribution matchLevelDistribution) {
            this.totalCvs = totalCvs;
            this.avgFinalScore = avgFinalScore;
            this.avgJobMatchScore = avgJobMatchScore;
            this.avgOrganizationScore = avgOrganizationScore;
            this.maxFinalScore = maxFinalScore;
            this.minFinalScore = minFinalScore;
            this.matchLevelDistribution = matchLevelDistribution;
        }

        public long getTotalCvs() { return totalCvs; }
        public double getAvgFinalScore() { return avgFinalScore; }
        public double getAvgJobMatchScore() { return avgJobMatchScore; }
        public double getAvgOrganizationScore() { return avgOrganizationScore; }
        public double getMaxFinalScore() { return maxFinalScore; }
        public double getMinFinalScore() { return minFinalScore; }
        public MatchLevelDistribution getMatchLevelDistribution() { return matchLevelDistribution; }
    }

    public static class MatchLevelDistribution {
        private final long excellent;
        private final long good;
        private final long fair;
        private final long poor;

        public MatchLevelDistribution(long excellent, long good, long fair, long poor) {
            this.excellent = excellent;
            this.good = good;
            this.fair = fair;
            this.poor = poor;
        }

        public long getExcellent() { return excellent; }
        public long getGood() { return good; }
        public long getFair() { return fair; }
        public long getPoor() { return poor; }
    }

    public static class GlobalScoresSummary {
        private final long totalCvs;
        private final double avgFinalScore;
        private final double avgJobMatchScore;
        private final double avgOrganizationScore;
        private final long excellentMatches;
        private final long goodMatches;
        private final MatchLevelDistribution matchLevelDistribution;

        public GlobalScoresSummary(long totalCvs, double avgFinalScore, double avgJobMatchScore,
                                   double avgOrganizationScore, long excellentMatches, long goodMatches,
                                   MatchLevelDistribution matchLevelDistribution) {
            this.totalCvs = totalCvs;
            this.avgFinalScore = avgFinalScore;
            this.avgJobMatchScore = avgJobMatchScore;
            this.avgOrganizationScore = avgOrganizationScore;
            this.excellentMatches = excellentMatches;
            this.goodMatches = goodMatches;
            this.matchLevelDistribution = matchLevelDistribution;
        }

        public long getTotalCvs() { return totalCvs; }
        public double getAvgFinalScore() { return avgFinalScore; }
        public double getAvgJobMatchScore() { return avgJobMatchScore; }
        public double getAvgOrganizationScore() { return avgOrganizationScore; }
        public long getExcellentMatches() { return excellentMatches; }
        public long getGoodMatches() { return goodMatches; }
        public MatchLevelDistribution getMatchLevelDistribution() { return matchLevelDistribution; }
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
    }
}