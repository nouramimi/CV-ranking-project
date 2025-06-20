package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.entity.JobOffer;

import java.util.List;
import java.util.Optional;

public interface JobOfferServiceInterface {
    JobOffer create(JobOffer offer, String email); // Changed parameter name
    List<JobOffer> getAll(String email); // Changed
    List<JobOffer> getActiveOffers(String email); // Changed
    Optional<JobOffer> getById(Long id, String email); // Changed
    Optional<JobOffer> update(Long id, JobOffer updated, String email); // Changed
    boolean delete(Long id, String email); // Changed
    boolean deactivate(Long id, String email); // Changed
    boolean exists(Long id);
    String getJobDescription(Long jobOfferId, String email); // Changed
    Company getCompanyDetailsByJobOfferId(Long jobOfferId, String email); // Changed
}
