package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.CvRanking;

import java.util.List;

public interface EmailServiceInterface {

    void sendAcceptanceEmails(List<CvRanking> acceptedCandidates, Long jobOfferId);

    void sendSimpleAcceptanceEmail(CvRanking ranking, Long jobOfferId);
}
