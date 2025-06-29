package com.example.cvfilter.dao.repository;

import com.example.cvfilter.dao.entity.CvScores;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT COUNT(cs) FROM CvScores cs WHERE FUNCTION('DATE', cs.processedAt) = CURRENT_DATE")
    Long countCvsScoredToday();

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

    // PAGEABLE METHODS
    @Query("SELECT cs FROM CvScores cs WHERE cs.jobOfferId = :jobOfferId " +
            "ORDER BY cs.finalScore DESC, cs.jobMatchScore DESC")
    Page<CvScores> findByJobOfferIdOrderByFinalScoreDesc(@Param("jobOfferId") Long jobOfferId, Pageable pageable);

    @Query("SELECT cs FROM CvScores cs WHERE cs.jobOfferId = :jobOfferId " +
            "ORDER BY cs.finalScore DESC, cs.jobMatchScore DESC, cs.organizationScore DESC")
    Page<CvScores> findTopCvsForJobByFinalScore(@Param("jobOfferId") Long jobOfferId, Pageable pageable);

    @Query("SELECT cs FROM CvScores cs WHERE cs.finalScore >= :minScore " +
            "ORDER BY cs.finalScore DESC")
    Page<CvScores> findByFinalScoreGreaterThanEqual(@Param("minScore") BigDecimal minScore, Pageable pageable);

    @Query("SELECT cs FROM CvScores cs WHERE cs.userId = :userId AND cs.finalScore >= :minScore " +
            "ORDER BY cs.finalScore DESC")
    Page<CvScores> findByUserIdAndFinalScoreGreaterThanEqual(
            @Param("userId") Long userId,
            @Param("minScore") BigDecimal minScore,
            Pageable pageable);

    @Query("SELECT cs FROM CvScores cs WHERE cs.userId = :userId AND cs.jobOfferId = :jobOfferId " +
            "ORDER BY cs.processedAt DESC")
    Page<CvScores> findByUserIdAndJobOfferIdOrderByProcessedAtDesc(
            @Param("userId") Long userId,
            @Param("jobOfferId") Long jobOfferId,
            Pageable pageable);

    @Query("SELECT cs FROM CvScores cs WHERE cs.matchLevel = :matchLevel " +
            "ORDER BY cs.finalScore DESC")
    Page<CvScores> findByMatchLevelOrderByFinalScoreDesc(@Param("matchLevel") String matchLevel, Pageable pageable);

    @Query("SELECT cs FROM CvScores cs WHERE cs.userId = :userId AND cs.matchLevel = :matchLevel " +
            "ORDER BY cs.finalScore DESC")
    Page<CvScores> findByUserIdAndMatchLevelOrderByFinalScoreDesc(
            @Param("userId") Long userId,
            @Param("matchLevel") String matchLevel,
            Pageable pageable);

    @Query("SELECT COUNT(cs) + 1 FROM CvScores cs " +
            "WHERE cs.jobOfferId = :jobOfferId " +
            "AND cs.finalScore > :finalScore")
    Long getCvRankForJobByFinalScore(
            @Param("jobOfferId") Long jobOfferId,
            @Param("finalScore") BigDecimal finalScore);

    Page<CvScores> findByUserIdOrderByProcessedAtDesc(Long userId, Pageable pageable);

    // COMPANY-FILTERED METHODS (FIXED TO USE companyId)
    @Query("SELECT cs FROM CvScores cs " +
            "WHERE cs.finalScore >= :minScore " +
            "AND cs.jobOfferId IN (SELECT jo.id FROM JobOffer jo WHERE jo.companyId = :companyId) " +
            "ORDER BY cs.finalScore DESC")
    Page<CvScores> findByFinalScoreGreaterThanEqualAndCompany(
            @Param("minScore") BigDecimal minScore,
            @Param("companyId") Long companyId,
            Pageable pageable);

    @Query("SELECT cs FROM CvScores cs " +
            "WHERE cs.jobOfferId IN (SELECT jo.id FROM JobOffer jo WHERE jo.companyId = :companyId) " +
            "ORDER BY cs.processedAt DESC")
    Page<CvScores> findAllByCompany(@Param("companyId") Long companyId, Pageable pageable);

    @Query("SELECT cs FROM CvScores cs " +
            "WHERE cs.matchLevel = :matchLevel " +
            "AND cs.jobOfferId IN (SELECT jo.id FROM JobOffer jo WHERE jo.companyId = :companyId) " +
            "ORDER BY cs.finalScore DESC")
    Page<CvScores> findByMatchLevelAndCompanyOrderByFinalScoreDesc(
            @Param("matchLevel") String matchLevel,
            @Param("companyId") Long companyId,
            Pageable pageable);

    @Query("SELECT COUNT(cs) > 0 FROM CvScores cs " +
            "WHERE cs.userId = :userId " +
            "AND cs.jobOfferId IN (SELECT jo.id FROM JobOffer jo WHERE jo.companyId = :companyId)")
    boolean existsUserInCompany(@Param("userId") Long userId, @Param("companyId") Long companyId);
}