package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.CvRanking;

import java.util.List;

public interface CvRankingServiceInterface {

    List<CvRanking> getBestCvsForJob(Long jobOfferId);
    List<CvRanking> getTopCvsForJob(Long jobOfferId, int topN);
}