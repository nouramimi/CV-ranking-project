package com.example.cvfilter.dao.repository;

import com.example.cvfilter.dao.entity.CvInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CvInfoRepository extends JpaRepository<CvInfo, Long> {
    List<CvInfo> findByJobOfferId(Long jobOfferId);
    List<CvInfo> findByUserId(Long userId);
    List<CvInfo> findByJobOfferIdAndUserId(Long jobOfferId, Long userId);
    boolean existsByJobOfferIdAndUserId(Long jobOfferId, Long userId);

    @Query("SELECT ci FROM CvInfo ci WHERE ci.jobOfferId = :jobOfferId AND ci.userId = :userId ORDER BY ci.extractedAt DESC")
    Optional<CvInfo> findLatestByJobOfferIdAndUserId(@Param("jobOfferId") Long jobOfferId, @Param("userId") Long userId);

    @Query("SELECT DISTINCT ci.userId FROM CvInfo ci WHERE ci.jobOfferId = :jobOfferId")
    List<Long> findDistinctUserIdsByJobOfferId(@Param("jobOfferId") Long jobOfferId);

    @Query("SELECT COUNT(DISTINCT ci.userId) FROM CvInfo ci WHERE ci.jobOfferId = :jobOfferId")
    long countDistinctUsersByJobOfferId(@Param("jobOfferId") Long jobOfferId);

    @Query("SELECT DISTINCT ci.jobOfferId FROM CvInfo ci WHERE ci.userId = :userId")
    List<Long> findDistinctJobOfferIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT ci FROM CvInfo ci WHERE ci.jobOfferId = :jobOfferId AND (ci.name IS NOT NULL OR ci.email IS NOT NULL)")
    List<CvInfo> findByJobOfferIdWithExtractedInfo(@Param("jobOfferId") Long jobOfferId);

    List<CvInfo> findByCompanyId(Long companyId);

    @Query("SELECT ci FROM CvInfo ci WHERE ci.companyId = :companyId AND (ci.name IS NOT NULL OR ci.email IS NOT NULL)")
    List<CvInfo> findByCompanyIdWithExtractedInfo(@Param("companyId") Long companyId);

    @Query("SELECT ci FROM CvInfo ci WHERE ci.jobOfferId = :jobOfferId AND LOWER(ci.skills) LIKE LOWER(CONCAT('%', :skill, '%'))")
    List<CvInfo> findByJobOfferIdAndSkillsContaining(@Param("jobOfferId") Long jobOfferId, @Param("skill") String skill);

    @Query("SELECT ci FROM CvInfo ci WHERE ci.jobOfferId = :jobOfferId AND LOWER(ci.experience) LIKE LOWER(CONCAT('%', :experience, '%'))")
    List<CvInfo> findByJobOfferIdAndExperienceContaining(@Param("jobOfferId") Long jobOfferId, @Param("experience") String experience);

    @Query("SELECT ci FROM CvInfo ci WHERE ci.jobOfferId = :jobOfferId AND LOWER(ci.education) LIKE LOWER(CONCAT('%', :education, '%'))")
    List<CvInfo> findByJobOfferIdAndEducationContaining(@Param("jobOfferId") Long jobOfferId, @Param("education") String education);

    @Query("SELECT ci FROM CvInfo ci WHERE ci.jobOfferId = :jobOfferId")
    List<CvInfo> findByJobOfferIdWithAllFields(@Param("jobOfferId") Long jobOfferId);

    @Query("SELECT c FROM CvInfo c WHERE c.jobOfferId = :jobOfferId")
    Page<CvInfo> findByJobOfferIdWithAllFields(@Param("jobOfferId") Long jobOfferId, Pageable pageable);

    // Paginated version for distinct job offers by user
    @Query("SELECT DISTINCT c.jobOfferId FROM CvInfo c WHERE c.userId = :userId")
    Page<Long> findDistinctJobOfferIdsByUserId(@Param("userId") Long userId, Pageable pageable);
}
