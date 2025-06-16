package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.JobOffer;

import java.util.List;
import java.util.Optional;

public interface JobOfferServiceInterface {

    JobOffer create(JobOffer offer);

    List<JobOffer> getAll();

    List<JobOffer> getActiveOffers();

    Optional<JobOffer> getById(Long id);

    Optional<JobOffer> update(Long id, JobOffer updated);

    boolean delete(Long id);

    boolean deactivate(Long id);

    boolean exists(Long id);

    String getJobDescription(Long jobOfferId);
}
