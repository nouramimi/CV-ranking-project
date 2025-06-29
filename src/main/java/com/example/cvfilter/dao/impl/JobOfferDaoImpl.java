package com.example.cvfilter.dao.impl;

import com.example.cvfilter.dao.JobOfferDao;
import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dao.repository.JobOfferRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JobOfferDaoImpl implements JobOfferDao {

    private final JobOfferRepository jobOfferRepository;

    public JobOfferDaoImpl(JobOfferRepository jobOfferRepository) {
        this.jobOfferRepository = jobOfferRepository;
    }

    @Override
    public JobOffer save(JobOffer offer) {
        return jobOfferRepository.save(offer);
    }

    @Override
    public List<JobOffer> findAll() {
        return jobOfferRepository.findAll();
    }

    @Override
    public List<JobOffer> findActiveOffers() {
        return jobOfferRepository.findByIsActive(true);
    }

    @Override
    public Optional<JobOffer> findById(Long id) {
        return jobOfferRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jobOfferRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jobOfferRepository.existsById(id);
    }

    @Override
    public Page<JobOffer> findAllWithFilters(Boolean active, JobOffer.EmploymentType employmentType, Double salary, String companyName, String jobTitle, Pageable pageable) {
        return jobOfferRepository.findAllWithFilters(active, employmentType, salary, companyName, jobTitle, pageable);
    }

    @Override
    public Page<JobOffer> findByCompanyIdAndFilters(Long companyId, Boolean active, JobOffer.EmploymentType employmentType, Double salary, String companyName, String jobTitle, Pageable pageable) {
        return jobOfferRepository.findByCompanyIdAndFilters(companyId, active, employmentType, salary, companyName, jobTitle, pageable);
    }

    @Override
    public boolean existsByIdAndCompanyId(Long jobOfferId, Long companyId) {
        return jobOfferRepository.existsByIdAndCompanyId(jobOfferId, companyId);
    }


}
