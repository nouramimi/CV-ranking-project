package com.example.cvfilter.dao;

import com.example.cvfilter.dao.entity.JobOffer;

import java.util.List;
import java.util.Optional;

public interface JobOfferDao {

    JobOffer save(JobOffer offer);

    List<JobOffer> findAll();

    List<JobOffer> findActiveOffers();

    Optional<JobOffer> findById(Long id);

    void deleteById(Long id);

    boolean existsById(Long id);
}
