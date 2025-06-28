package com.example.cvfilter.dao.repository;

import com.example.cvfilter.dao.entity.CvScores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CvScoresRepository extends JpaRepository<CvScores, Long> {

    Optional<CvScores> findByUserIdAndJobOfferId(Long userId, Long jobOfferId);

    boolean existsByUserIdAndJobOfferId(Long userId, Long jobOfferId);

    List<CvScores> findByJobOfferIdOrderByOrganizationScoreDesc(Long jobOfferId);

    List<CvScores> findByUserIdOrderByProcessedAtDesc(Long userId);

    @Query("SELECT cs FROM CvScores cs WHERE cs.organizationScore >= :minScore ORDER BY cs.organizationScore DESC")
    List<CvScores> findByOrganizationScoreGreaterThanEqual(@Param("minScore") BigDecimal minScore);

    @Query("SELECT cs FROM CvScores cs WHERE cs.jobOfferId = :jobOfferId " +
            "ORDER BY cs.organizationScore DESC, cs.technicalScore DESC")
    List<CvScores> findTopCvsForJob(@Param("jobOfferId") Long jobOfferId);

    @Query("SELECT " +
            "AVG(cs.organizationScore) as avgOrgScore, " +
            "MAX(cs.organizationScore) as maxOrgScore, " +
            "MIN(cs.organizationScore) as minOrgScore, " +
            "COUNT(cs) as totalCvs " +
            "FROM CvScores cs WHERE cs.jobOfferId = :jobOfferId")
    Object[] getJobOfferStats(@Param("jobOfferId") Long jobOfferId);

    @Query("SELECT cs FROM CvScores cs WHERE cs.organizationScore IS NULL OR cs.technicalScore IS NULL")
    List<CvScores> findUnscoredCvs();

    // FIXED: Use JPQL date comparison instead of DATE() function
    @Query("SELECT COUNT(cs) FROM CvScores cs WHERE FUNCTION('DATE', cs.processedAt) = CURRENT_DATE")
    Long countCvsScoredToday();

    // ALTERNATIVE 1: Use date range comparison (more portable)
    @Query("SELECT COUNT(cs) FROM CvScores cs WHERE cs.processedAt >= :startOfDay AND cs.processedAt < :startOfNextDay")
    Long countCvsScoredTodayWithRange(@Param("startOfDay") LocalDateTime startOfDay, @Param("startOfNextDay") LocalDateTime startOfNextDay);

    // ALTERNATIVE 2: Use derived query method (Spring Data JPA will handle it)
    Long countByProcessedAtBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    void deleteByUserIdAndJobOfferId(Long userId, Long jobOfferId);

    @Query("SELECT cs FROM CvScores cs WHERE cs.compositeScore >= :threshold " +
            "ORDER BY cs.compositeScore DESC, cs.organizationScore DESC")
    List<CvScores> findHighScoringCvs(@Param("threshold") BigDecimal threshold);

    @Query("SELECT COUNT(cs) + 1 FROM CvScores cs " +
            "WHERE cs.jobOfferId = :jobOfferId " +
            "AND (cs.organizationScore > :orgScore " +
            "     OR (cs.organizationScore = :orgScore AND cs.technicalScore > :techScore))")
    Long getCvRankForJob(@Param("jobOfferId") Long jobOfferId,
                         @Param("orgScore") BigDecimal orgScore,
                         @Param("techScore") BigDecimal techScore);
}
