package com.example.cvfilter.dao.impl;

import com.example.cvfilter.dao.CvInfoDao;
import com.example.cvfilter.dao.entity.CvInfo;
import com.example.cvfilter.dao.repository.CvInfoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CvInfoDaoImpl implements CvInfoDao {

    private final CvInfoRepository cvInfoRepository;

    public CvInfoDaoImpl(CvInfoRepository cvInfoRepository) {
        this.cvInfoRepository = cvInfoRepository;
    }

    @Override
    public List<CvInfo> findByJobOfferId(Long jobOfferId) {
        return cvInfoRepository.findByJobOfferId(jobOfferId);
    }

    @Override
    public List<CvInfo> findByUserId(Long userId) {
        return cvInfoRepository.findByUserId(userId);
    }

    @Override
    public List<CvInfo> findByJobOfferIdAndUserId(Long jobOfferId, Long userId) {
        return cvInfoRepository.findByJobOfferIdAndUserId(jobOfferId, userId);
    }

    @Override
    public boolean existsByJobOfferIdAndUserId(Long jobOfferId, Long userId) {
        return cvInfoRepository.existsByJobOfferIdAndUserId(jobOfferId, userId);
    }

    @Override
    public Optional<CvInfo> findLatestByJobOfferIdAndUserId(Long jobOfferId, Long userId) {
        return cvInfoRepository.findLatestByJobOfferIdAndUserId(jobOfferId, userId);
    }

    @Override
    public List<Long> findDistinctUserIdsByJobOfferId(Long jobOfferId) {
        return cvInfoRepository.findDistinctUserIdsByJobOfferId(jobOfferId);
    }

    @Override
    public long countDistinctUsersByJobOfferId(Long jobOfferId) {
        return cvInfoRepository.countDistinctUsersByJobOfferId(jobOfferId);
    }

    @Override
    public List<Long> findDistinctJobOfferIdsByUserId(Long userId) {
        return cvInfoRepository.findDistinctJobOfferIdsByUserId(userId);
    }

    @Override
    public List<CvInfo> findByJobOfferIdWithExtractedInfo(Long jobOfferId) {
        return cvInfoRepository.findByJobOfferIdWithExtractedInfo(jobOfferId);
    }

    @Override
    public List<CvInfo> findByCompanyId(Long companyId) {
        return cvInfoRepository.findByCompanyId(companyId);
    }

    @Override
    public List<CvInfo> findByCompanyIdWithExtractedInfo(Long companyId) {
        return cvInfoRepository.findByCompanyIdWithExtractedInfo(companyId);
    }

    @Override
    public List<CvInfo> findByJobOfferIdAndSkillsContaining(Long jobOfferId, String skill) {
        return cvInfoRepository.findByJobOfferIdAndSkillsContaining(jobOfferId, skill);
    }

    @Override
    public List<CvInfo> findByJobOfferIdAndExperienceContaining(Long jobOfferId, String experience) {
        return cvInfoRepository.findByJobOfferIdAndExperienceContaining(jobOfferId, experience);
    }

    @Override
    public List<CvInfo> findByJobOfferIdAndEducationContaining(Long jobOfferId, String education) {
        return cvInfoRepository.findByJobOfferIdAndEducationContaining(jobOfferId, education);
    }

    @Override
    public CvInfo save(CvInfo cvInfo) {
        return cvInfoRepository.save(cvInfo);
    }

    @Override
    public Optional<CvInfo> findById(Long id) {
        return cvInfoRepository.findById(id);
    }

    @Override
    public List<CvInfo> findByJobOfferIdWithAllFields(Long jobOfferId) {
        return cvInfoRepository.findByJobOfferIdWithAllFields(jobOfferId);
    }

    @Override
    public Page<CvInfo> findByJobOfferIdWithAllFields(Long jobOfferId, Pageable pageable) {
        return cvInfoRepository.findByJobOfferIdWithAllFields(jobOfferId, pageable);
    }

    @Override
    public Page<Long> findDistinctJobOfferIdsByUserId(Long userId, Pageable pageable) {
        return cvInfoRepository.findDistinctJobOfferIdsByUserId(userId, pageable);
    }

}