package com.example.cvfilter.dao;

import com.example.cvfilter.dao.entity.CvInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CvInfoDao {
    List<CvInfo> findByJobOfferId(Long jobOfferId);
    List<CvInfo> findByUserId(Long userId);
    List<CvInfo> findByJobOfferIdAndUserId(Long jobOfferId, Long userId);
    boolean existsByJobOfferIdAndUserId(Long jobOfferId, Long userId);
    Optional<CvInfo> findLatestByJobOfferIdAndUserId(Long jobOfferId, Long userId);
    List<Long> findDistinctUserIdsByJobOfferId(Long jobOfferId);
    long countDistinctUsersByJobOfferId(Long jobOfferId);
    List<Long> findDistinctJobOfferIdsByUserId(Long userId);
    List<CvInfo> findByJobOfferIdWithExtractedInfo(Long jobOfferId);
    List<CvInfo> findByCompanyId(Long companyId);
    List<CvInfo> findByCompanyIdWithExtractedInfo(Long companyId);
    List<CvInfo> findByJobOfferIdAndSkillsContaining(Long jobOfferId, String skill);
    List<CvInfo> findByJobOfferIdAndExperienceContaining(Long jobOfferId, String experience);
    List<CvInfo> findByJobOfferIdAndEducationContaining(Long jobOfferId, String education);
    CvInfo save(CvInfo cvInfo);
    Optional<CvInfo> findById(Long id);
    @Query("SELECT ci FROM CvInfo ci WHERE ci.jobOfferId = :jobOfferId")
    List<CvInfo> findByJobOfferIdWithAllFields(@Param("jobOfferId") Long jobOfferId);
}