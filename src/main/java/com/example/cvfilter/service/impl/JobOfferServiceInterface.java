package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dto.JobOfferDTO;
import com.example.cvfilter.dto.JobOfferWithCompanyDTO;
import com.example.cvfilter.dto.PaginatedResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface JobOfferServiceInterface {
    JobOfferDTO create(JobOffer offer, String email);
    List<JobOfferDTO> getAll(String email);
    List<JobOfferDTO> getActiveOffers(String email);
    Optional<JobOfferDTO> getById(Long id, String email);
    Optional<JobOfferDTO> update(Long id, JobOffer updated, String email);
    boolean delete(Long id, String email);
    boolean deactivate(Long id, String email);
    boolean exists(Long id);
    String getJobDescription(Long jobOfferId, String email);
    Company getCompanyDetailsByJobOfferId(Long jobOfferId, String email);
    Optional<JobOfferWithCompanyDTO> getByIdWithCompany(Long id, String email);
    //List<JobOfferWithCompanyDTO> getAllJobOffersWithCompanyInfo(String email, Boolean active);
    //PaginatedResponse<JobOfferWithCompanyDTO> getAllJobOffersWithCompanyInfo(
    //        String email, Boolean active, int page, int size);
    PaginatedResponse<JobOfferWithCompanyDTO> getAllJobOffersWithCompanyInfo(
            String email, Boolean active, JobOffer.EmploymentType employmentType,
            Double salary, String companyName, String jobTitle, int page, int size);
    JobOffer parseJobOfferFromDescription(String jobTitle, String jobDescription);
    Set<String> extractSkillsFromDescription(String description);
    Double extractExperienceFromDescription(String description);
    String extractEducationFromDescription(String description);




}
