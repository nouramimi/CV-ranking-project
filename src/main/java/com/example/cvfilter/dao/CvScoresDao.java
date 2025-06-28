/*package com.example.cvfilter.dao;

import com.example.cvfilter.dao.entity.CvScores;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CvScoresDao {
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

    @Query("SELECT COUNT(cs) FROM CvScores cs WHERE DATE(cs.processedAt) = CURRENT_DATE")
    Long countCvsScoredToday();

    void deleteByUserIdAndJobOfferId(Long userId, Long jobOfferId);

    @Query("SELECT cs FROM CvScores cs WHERE cs.compositeScore >= :threshold " +
            "ORDER BY cs.compositeScore DESC, cs.organizationScore DESC")
    List<CvScores> findHighScoringCvs(@Param("threshold") BigDecimal threshold);


    @Query("SELECT COUNT(cs) + 1 FROM CvScores cs " +
            "WHERE cs.jobOfferId = :jobOfferId " +
            "AND (cs.organizationScore > :orgScore " +
            "     OR (cs.organizationScore = :orgScore AND cs.technicalScore > :techScore))")
    Long getCvRankForJob(@Param("jobOfferId") Long jobOfferId,                  @Param("orgScore") BigDecimal orgScore,
                         @Param("techScore") BigDecimal techScore);
    List<CvScores> findAll();


}*/


