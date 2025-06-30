package com.example.cvfilter.controller;

import com.example.cvfilter.config.JwtUtils;
import com.example.cvfilter.dao.entity.Company;
import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dto.JobOfferDTO;
import com.example.cvfilter.dto.JobOfferCreateDTO;
import com.example.cvfilter.dto.JobOfferWithCompanyDTO;
import com.example.cvfilter.dto.PaginatedResponse;
import com.example.cvfilter.exception.JobOfferNotFoundException;
import com.example.cvfilter.service.impl.JobOfferServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<JobOfferDTO> create(@RequestBody JobOfferCreateDTO jobOfferCreateDTO,
                                              HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        JobOfferDTO createdOffer = service.create(jobOfferCreateDTO, email);
        return ResponseEntity.ok(createdOffer);
    }

    @GetMapping("/getAll")
    public ResponseEntity<PaginatedResponse<JobOfferWithCompanyDTO>> getAll(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) JobOffer.EmploymentType employmentType,
            @RequestParam(required = false) Double salary,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        String email = extractEmailFromRequest(request);
        PaginatedResponse<JobOfferWithCompanyDTO> response =
                service.getAllJobOffersWithCompanyInfo(email, active, employmentType,
                        salary, companyName, jobTitle, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<JobOfferDTO> getById(@PathVariable Long id, HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        return service.getById(id, email)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found with ID: " + id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<JobOfferDTO> update(@PathVariable Long id,
                                              @RequestBody JobOfferDTO updated,
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
        String email = extractEmailFromRequest(request);
        String description = service.getJobDescription(id, email);
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