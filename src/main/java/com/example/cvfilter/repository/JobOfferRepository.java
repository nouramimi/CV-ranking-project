package com.example.cvfilter.repository;

import com.example.cvfilter.model.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {
    List<JobOffer> findByIsActive(boolean isActive);
}