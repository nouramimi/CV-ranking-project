package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.CvRanking;
import com.example.cvfilter.controller.CvRankingController.CvRankingDetails;

import java.util.List;

public interface CvRankingServiceInterface {
    List<CvRanking> getTopCvsForJob(Long jobOfferId, int topN, String username);
    List<CvRanking> getBestCvsForJob(Long jobOfferId, String username);
    int getBestCvsAndNotify(Long jobOfferId, String username);
    int getTopCvsAndNotify(Long jobOfferId, int topN, String username);
    CvRanking getRankingDetails(Long jobOfferId, String username);
}