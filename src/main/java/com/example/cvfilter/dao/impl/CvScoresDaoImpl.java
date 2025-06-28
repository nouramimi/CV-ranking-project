/*package com.example.cvfilter.dao.impl;

import com.example.cvfilter.dao.CvScoresDao;
import com.example.cvfilter.dao.entity.CvScores;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class CvScoresDaoImpl implements CvScoresDao {

    private final CvScoresDao cvScoresDao;

    public CvScoresDaoImpl(CvScoresDao cvScoresDao) {
        this.cvScoresDao = cvScoresDao;
    }

    @Override
    public Optional<CvScores> findByUserIdAndJobOfferId(Long userId, Long jobOfferId) {
        return cvScoresDao.findByUserIdAndJobOfferId(userId, jobOfferId);
    }

    @Override
    public boolean existsByUserIdAndJobOfferId(Long userId, Long jobOfferId) {
        return cvScoresDao.existsByUserIdAndJobOfferId(userId, jobOfferId);
    }

    @Override
    public List<CvScores> findByJobOfferIdOrderByOrganizationScoreDesc(Long jobOfferId) {
        return cvScoresDao.findByJobOfferIdOrderByOrganizationScoreDesc(jobOfferId);
    }

    @Override
    public List<CvScores> findByUserIdOrderByProcessedAtDesc(Long userId) {
        return cvScoresDao.findByUserIdOrderByProcessedAtDesc(userId);
    }

    @Override
    public List<CvScores> findByOrganizationScoreGreaterThanEqual(BigDecimal minScore) {
        return cvScoresDao.findByOrganizationScoreGreaterThanEqual(minScore);
    }

    @Override
    public List<CvScores> findTopCvsForJob(Long jobOfferId) {
        return cvScoresDao.findTopCvsForJob(jobOfferId);
    }

    @Override
    public Object[] getJobOfferStats(Long jobOfferId) {
        return cvScoresDao.getJobOfferStats(jobOfferId);
    }

    @Override
    public List<CvScores> findUnscoredCvs() {
        return cvScoresDao.findUnscoredCvs();
    }

    @Override
    public Long countCvsScoredToday() {
        return cvScoresDao.countCvsScoredToday();
    }

    @Override
    public void deleteByUserIdAndJobOfferId(Long userId, Long jobOfferId) {
        cvScoresDao.deleteByUserIdAndJobOfferId(userId, jobOfferId);
    }

    @Override
    public List<CvScores> findHighScoringCvs(BigDecimal threshold) {
        return cvScoresDao.findHighScoringCvs(threshold);
    }

    @Override
    public Long getCvRankForJob(Long jobOfferId, BigDecimal orgScore, BigDecimal techScore) {
        return cvScoresDao.getCvRankForJob(jobOfferId, orgScore, techScore);
    }

    @Override
    public List<CvScores> findAll() {
        return cvScoresDao.findAll();
    }
}*/
