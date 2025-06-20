package com.example.cvfilter.controller;

import com.example.cvfilter.config.JwtUtils;
import com.example.cvfilter.dao.entity.CvInfo;
import com.example.cvfilter.dto.CvInfoDTO;
import com.example.cvfilter.service.AuthorizationService;
import com.example.cvfilter.service.CvUploadService;
import com.example.cvfilter.service.impl.AuthorizationServiceInterface;
import com.example.cvfilter.service.impl.CvUploadServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cv")
public class CvUploadController {

    private final CvUploadServiceInterface cvUploadService;
    private final JwtUtils jwtUtils;
    private final AuthorizationServiceInterface authorizationService;

    public CvUploadController(CvUploadServiceInterface cvUploadService,
                              JwtUtils jwtUtils,
                              AuthorizationServiceInterface authorizationService) {
        this.cvUploadService = cvUploadService;
        this.jwtUtils = jwtUtils;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/upload/{jobId}")
    public ResponseEntity<String> uploadCv(@PathVariable Long jobId,
                                           @RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) throws IOException {
        String email = extractEmailFromRequest(request); // Changed from username to email
        String path = cvUploadService.uploadCv(jobId, file, email); // Changed parameter
        return ResponseEntity.ok("CV uploaded to: " + path);
    }

    @GetMapping("/job/{jobId}/candidates")
    public ResponseEntity<List<CvInfoDTO>> getCandidatesForJob(@PathVariable Long jobId) {
        List<CvInfo> cvInfos = cvUploadService.getCandidatesForJobOffer(jobId);
        List<CvInfoDTO> dtos = cvInfos.stream()
                .map(CvInfoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/job/{jobId}/unique-candidates")
    public ResponseEntity<List<CvInfo>> getUniqueCandidatesForJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(cvUploadService.getUniqueCandidatesForJobOffer(jobId));
    }

    @GetMapping("/job/{jobId}/candidate-count")
    public ResponseEntity<Long> getCandidateCountForJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(cvUploadService.getCandidateCountForJobOffer(jobId));
    }

    @GetMapping("/job/{jobId}/extracted-info")
    public ResponseEntity<List<CvInfo>> getCandidatesWithExtractedInfo(@PathVariable Long jobId) {
        return ResponseEntity.ok(cvUploadService.getCandidatesWithExtractedInfo(jobId));
    }

    @GetMapping("/user/applications")
    public ResponseEntity<List<Long>> getUserApplications(HttpServletRequest request) {
        String email = extractEmailFromRequest(request); // Changed
        Long userId = authorizationService.getUserIdByEmail(email); // Changed
        return ResponseEntity.ok(cvUploadService.getJobOffersForUser(userId));
    }

    @GetMapping("/user/has-applied/{jobId}")
    public ResponseEntity<Boolean> hasUserAppliedToJob(@PathVariable Long jobId,
                                                       HttpServletRequest request) {
        String email = extractEmailFromRequest(request); // Changed
        Long userId = authorizationService.getUserIdByEmail(email); // Changed
        return ResponseEntity.ok(cvUploadService.hasUserAppliedToJob(jobId, userId));
    }

    @GetMapping("/user/cvs")
    public ResponseEntity<List<CvInfo>> getUserCvs(HttpServletRequest request) {
        String email = extractEmailFromRequest(request); // Changed
        Long userId = authorizationService.getUserIdByEmail(email); // Changed
        return ResponseEntity.ok(cvUploadService.getCvInfosByUser(userId));
    }

    @GetMapping("/company/{companyId}/cvs")
    public ResponseEntity<List<CvInfo>> getCompanyCvs(@PathVariable Long companyId) {
        return ResponseEntity.ok(cvUploadService.getCvInfosByCompany(companyId));
    }

    // Méthodes de recherche avancée
    @GetMapping("/job/{jobId}/search/skills")
    public ResponseEntity<List<CvInfo>> searchBySkills(@PathVariable Long jobId,
                                                       @RequestParam String skill) {
        return ResponseEntity.ok(cvUploadService.searchCandidatesBySkills(jobId, skill));
    }

    @GetMapping("/job/{jobId}/search/experience")
    public ResponseEntity<List<CvInfo>> searchByExperience(@PathVariable Long jobId,
                                                           @RequestParam String experience) {
        return ResponseEntity.ok(cvUploadService.searchCandidatesByExperience(jobId, experience));
    }

    @GetMapping("/job/{jobId}/search/education")
    public ResponseEntity<List<CvInfo>> searchByEducation(@PathVariable Long jobId,
                                                          @RequestParam String education) {
        return ResponseEntity.ok(cvUploadService.searchCandidatesByEducation(jobId, education));
    }

    // Méthode pour mettre à jour les informations extraites
    @PutMapping("/{cvInfoId}/update-info")
    public ResponseEntity<Void> updateCvInfo(@PathVariable Long cvInfoId,
                                             @RequestParam(required = false) String name,
                                             @RequestParam(required = false) String email,
                                             @RequestParam(required = false) String phone,
                                             @RequestParam(required = false) String description,
                                             @RequestParam(required = false) String skills,
                                             @RequestParam(required = false) String experience,
                                             @RequestParam(required = false) String education) {
        cvUploadService.updateCvInfo(cvInfoId, name, email, phone, description, skills, experience, education);
        return ResponseEntity.ok().build();
    }

    private String extractEmailFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String email = jwtUtils.extractEmail(token); // Changed to extract email
        if (email == null) {
            throw new IllegalArgumentException("Unable to extract email from token");
        }
        return email;
    }
}