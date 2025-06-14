package com.example.cvfilter.service;

import com.example.cvfilter.model.JobOffer;
import com.example.cvfilter.repository.JobOfferRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class JobOfferService {

    private final JobOfferRepository repository;

    public JobOfferService(JobOfferRepository repository) {
        this.repository = repository;
    }

    public JobOffer create(JobOffer offer) {
        validateJobOffer(offer);
        return repository.save(offer);
    }

    public List<JobOffer> getAll() {
        return repository.findAll();
    }

    public List<JobOffer> getActiveOffers() {
        return repository.findByIsActive(true);
    }

    public Optional<JobOffer> getById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return repository.findById(id);
    }

    public Optional<JobOffer> update(Long id, JobOffer updated) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }

        return repository.findById(id)
                .map(existing -> {
                    updateJobOfferFields(existing, updated);
                    return repository.save(existing);
                });
    }

    public boolean delete(Long id) {
        if (id == null || id <= 0) {
            return false;
        }

        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean deactivate(Long id) {
        return repository.findById(id)
                .map(offer -> {
                    offer.setActive(false);
                    repository.save(offer);
                    return true;
                })
                .orElse(false);
    }

    public boolean exists(Long id) {
        return id != null && id > 0 && repository.existsById(id);
    }

    public String getJobDescription(Long jobOfferId) {
        return getById(jobOfferId)
                .map(JobOffer::getDescription)
                .orElse(null);
    }

    private void validateJobOffer(JobOffer offer) {
        if (offer.getTitle() == null || offer.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Job title cannot be empty");
        }

        if (offer.getDescription() == null || offer.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Job description cannot be empty");
        }

        if (offer.getLocation() == null || offer.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Job location cannot be empty");
        }

        if (offer.getEmploymentType() == null) {
            throw new IllegalArgumentException("Employment type must be specified");
        }

        if (offer.getPostingDate() == null) {
            offer.setPostingDate(LocalDate.now());
        }

        if (offer.getClosingDate() != null && offer.getClosingDate().isBefore(offer.getPostingDate())) {
            throw new IllegalArgumentException("Closing date cannot be before posting date");
        }

        if (offer.getMinSalary() != null && offer.getMaxSalary() != null
                && offer.getMinSalary() > offer.getMaxSalary()) {
            throw new IllegalArgumentException("Minimum salary cannot be greater than maximum salary");
        }

        if (offer.getContactEmail() == null || !offer.getContactEmail().contains("@")) {
            throw new IllegalArgumentException("Valid contact email is required");
        }

        // Set default values
        if (offer.getIsActive() == null) {
            offer.setActive(true);
        }
    }

    private void updateJobOfferFields(JobOffer existing, JobOffer updated) {
        if (updated.getTitle() != null && !updated.getTitle().trim().isEmpty()) {
            existing.setTitle(updated.getTitle().trim());
        }

        if (updated.getDescription() != null && !updated.getDescription().trim().isEmpty()) {
            existing.setDescription(updated.getDescription().trim());
        }

        if (updated.getDetailedDescription() != null) {
            existing.setDetailedDescription(updated.getDetailedDescription());
        }

        if (updated.getLocation() != null && !updated.getLocation().trim().isEmpty()) {
            existing.setLocation(updated.getLocation().trim());
        }

        if (updated.getDepartment() != null) {
            existing.setDepartment(updated.getDepartment());
        }

        if (updated.getEmploymentType() != null) {
            existing.setEmploymentType(updated.getEmploymentType());
        }

        if (updated.getPostingDate() != null) {
            existing.setPostingDate(updated.getPostingDate());
        }

        if (updated.getClosingDate() != null) {
            existing.setClosingDate(updated.getClosingDate());
        }

        if (updated.getMinSalary() != null) {
            existing.setMinSalary(updated.getMinSalary());
        }

        if (updated.getMaxSalary() != null) {
            existing.setMaxSalary(updated.getMaxSalary());
        }

        if (updated.getSalaryCurrency() != null) {
            existing.setSalaryCurrency(updated.getSalaryCurrency());
        }

        if (updated.getYearsOfExperienceRequired() != null) {
            existing.setYearsOfExperienceRequired(updated.getYearsOfExperienceRequired());
        }

        if (updated.getContactEmail() != null && updated.getContactEmail().contains("@")) {
            existing.setContactEmail(updated.getContactEmail());
        }

        if (updated.getContactPhone() != null) {
            existing.setContactPhone(updated.getContactPhone());
        }

        if (updated.getIsActive() != null) {
            existing.setActive(updated.getIsActive());
        }
    }
}