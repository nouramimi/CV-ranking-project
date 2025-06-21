package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dto.JobOfferWithCompanyDTO;

import java.util.List;
import java.util.Optional;

public interface JobOfferServiceInterface {
    JobOffer create(JobOffer offer, String email);
    List<JobOffer> getAll(String email);
    List<JobOffer> getActiveOffers(String email);
    Optional<JobOffer> getById(Long id, String email);
    Optional<JobOffer> update(Long id, JobOffer updated, String email); // Changed
    boolean delete(Long id, String email);
    boolean deactivate(Long id, String email);
    boolean exists(Long id);
    String getJobDescription(Long jobOfferId, String email);
    Company getCompanyDetailsByJobOfferId(Long jobOfferId, String email);
    Optional<JobOfferWithCompanyDTO> getByIdWithCompany(Long id, String email);
    List<JobOfferWithCompanyDTO> getAllJobOffersWithCompanyInfo(String email, Boolean active);
}
