package com.example.cvfilter.controller;

import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.exception.JobOfferNotFoundException;
import com.example.cvfilter.service.impl.JobOfferServiceInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-offers")
public class JobOfferController {

    private final JobOfferServiceInterface service;

    public JobOfferController(JobOfferServiceInterface service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<JobOffer> create(@RequestBody JobOffer offer) {
        JobOffer createdOffer = service.create(offer);
        return ResponseEntity.ok(createdOffer);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<JobOffer>> getAll(@RequestParam(required = false) Boolean active) {
        if (Boolean.TRUE.equals(active)) {
            return ResponseEntity.ok(service.getActiveOffers());
        }
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<JobOffer> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found with ID: " + id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<JobOffer> update(@PathVariable Long id, @RequestBody JobOffer updated) {
        return service.update(id, updated)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found with ID: " + id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        throw new JobOfferNotFoundException("Cannot delete: Job offer not found with ID: " + id);
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        if (service.deactivate(id)) {
            return ResponseEntity.noContent().build();
        }
        throw new JobOfferNotFoundException("Cannot deactivate: Job offer not found with ID: " + id);
    }

    @GetMapping("/description/{id}")
    public ResponseEntity<String> getJobDescription(@PathVariable Long id) {
        String description = service.getJobDescription(id);
        if (description != null) {
            return ResponseEntity.ok(description);
        }
        throw new JobOfferNotFoundException("Job description not found for ID: " + id);
    }
}
