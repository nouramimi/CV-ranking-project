package com.example.cvfilter.controller;

import com.example.cvfilter.config.JwtUtils;
import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dto.JobOfferWithCompanyDTO;
import com.example.cvfilter.exception.JobOfferNotFoundException;
import com.example.cvfilter.service.impl.JobOfferServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-offers")
public class JobOfferController {

    private final JobOfferServiceInterface service;
    private final JwtUtils jwtUtils;

    public JobOfferController(JobOfferServiceInterface service, JwtUtils jwtUtils) {
        this.service = service;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/create")
    public ResponseEntity<JobOffer> create(@RequestBody JobOffer offer, HttpServletRequest request) {
        String email = extractEmailFromRequest(request); // Changed from username to email
        JobOffer createdOffer = service.create(offer, email); // Changed parameter
        return ResponseEntity.ok(createdOffer);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<JobOfferWithCompanyDTO>> getAll(
            @RequestParam(required = false) Boolean active,
            HttpServletRequest request) {

        String email = extractEmailFromRequest(request);
        List<JobOfferWithCompanyDTO> response = service.getAllJobOffersWithCompanyInfo(email, active);
        return ResponseEntity.ok(response);
    }

    /*@GetMapping("/getAll")
    public ResponseEntity<List<JobOffer>> getAll(@RequestParam(required = false) Boolean active,
                                                 HttpServletRequest request) {
        String email = extractEmailFromRequest(request); // Changed

        if (Boolean.TRUE.equals(active)) {
            return ResponseEntity.ok(service.getActiveOffers(email)); // Changed
        }
        return ResponseEntity.ok(service.getAll(email)); // Changed
    }*/

    @GetMapping("/getById/{id}")
    public ResponseEntity<JobOfferWithCompanyDTO> getById(@PathVariable Long id, HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        return service.getByIdWithCompany(id, email)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found with ID: " + id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<JobOffer> update(@PathVariable Long id,
                                           @RequestBody JobOffer updated,
                                           HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        return service.update(id, updated, email)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found with ID: " + id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        if (service.delete(id, email)) {
            return ResponseEntity.noContent().build();
        }
        throw new JobOfferNotFoundException("Cannot delete: Job offer not found with ID: " + id);
    }

    @PatchMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id, HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        if (service.deactivate(id, email)) {
            return ResponseEntity.noContent().build();
        }
        throw new JobOfferNotFoundException("Cannot deactivate: Job offer not found with ID: " + id);
    }


    @GetMapping("/description/{id}")
    public ResponseEntity<String> getJobDescription(@PathVariable Long id, HttpServletRequest request) {
        String email = extractEmailFromRequest(request);String description = service.getJobDescription(id, email);
        if (description != null) {
            return ResponseEntity.ok(description);
        }
        throw new JobOfferNotFoundException("Job description not found for ID: " + id);
    }

    @GetMapping("/company-details/{id}")
    public ResponseEntity<Company> getCompanyDetails(@PathVariable Long id, HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        Company company = service.getCompanyDetailsByJobOfferId(id, email);
        if (company != null) {
            return ResponseEntity.ok(company);
        }
        throw new JobOfferNotFoundException("Company details not found for job offer ID: " + id);
    }

    private String extractEmailFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        String email = jwtUtils.extractEmail(token);

        if (email == null) {
            throw new IllegalArgumentException("Unable to extract email from token");
        }

        return email;
    }
}
