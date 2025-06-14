package com.example.cvfilter.controller;

import com.example.cvfilter.model.JobOffer;
import com.example.cvfilter.service.JobOfferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-offers")
public class JobOfferController {

    private final JobOfferService service;

    public JobOfferController(JobOfferService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<JobOffer> create(@RequestBody JobOffer offer) {
        try {
            JobOffer createdOffer = service.create(offer);
            return ResponseEntity.ok(createdOffer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<JobOffer>> getAll(
            @RequestParam(required = false) Boolean active) {
        if (active != null) {
            return ResponseEntity.ok(service.getActiveOffers());
        }
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobOffer> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobOffer> update(
            @PathVariable Long id,
            @RequestBody JobOffer updated) {
        try {
            return service.update(id, updated)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        if (service.deactivate(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/description")
    public ResponseEntity<String> getJobDescription(@PathVariable Long id) {
        String description = service.getJobDescription(id);
        if (description != null) {
            return ResponseEntity.ok(description);
        }
        return ResponseEntity.notFound().build();
    }
}