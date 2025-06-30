package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dto.JobOfferDTO;
import com.example.cvfilter.dto.JobOfferCreateDTO;
import com.example.cvfilter.dto.JobOfferWithCompanyDTO;
import com.example.cvfilter.dto.PaginatedResponse;

import java.util.List;
import java.util.Optional;

public interface JobOfferServiceInterface {
    JobOfferDTO create(JobOfferCreateDTO jobOfferCreateDTO, String email);

    PaginatedResponse<JobOfferWithCompanyDTO> getAllJobOffersWithCompanyInfo(
            String email, Boolean active, JobOffer.EmploymentType employmentType,
            Double salary, String companyName, String jobTitle, int page, int size);

    List<JobOfferDTO> getAll(String email);
    List<JobOfferDTO> getActiveOffers(String email);

    Optional<JobOfferDTO> getById(Long id, String email);
    Optional<JobOfferDTO> update(Long id, JobOfferDTO updated, String email);

    boolean delete(Long id, String email);
    boolean deactivate(Long id, String email);
    boolean exists(Long id);

    Optional<JobOfferWithCompanyDTO> getByIdWithCompany(Long id, String email);
    String getJobDescription(Long jobOfferId, String email);
    Company getCompanyDetailsByJobOfferId(Long jobOfferId, String email);

    JobOffer parseJobOfferFromDescription(String jobTitle, String jobDescription);
}
