package com.example.cvfilter.controller;

import com.example.cvfilter.config.JwtUtils;
import com.example.cvfilter.dao.UserDao;
import com.example.cvfilter.dao.entity.CvInfo;
import com.example.cvfilter.dao.entity.User;
import com.example.cvfilter.dto.*;
import com.example.cvfilter.exception.UserNotFoundException;
import com.example.cvfilter.service.CvUploadService;
import com.example.cvfilter.service.impl.AuthorizationServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/cv")
public class CvUploadController {

    private final CvUploadService cvUploadService;
    private final JwtUtils jwtUtils;
    private final AuthorizationServiceInterface authorizationService;
    private final UserDao userDao;

    public CvUploadController(CvUploadService cvUploadService, JwtUtils jwtUtils,
                              AuthorizationServiceInterface authorizationService, UserDao userDao) {
        this.cvUploadService = cvUploadService;
        this.jwtUtils = jwtUtils;
        this.authorizationService = authorizationService;
        this.userDao = userDao;
    }

    // === NOUVELLES MÉTHODES UTILISANT LES DTOs ===

    @PostMapping("/upload/{jobId}")
    public ResponseEntity<CvUploadResponseDto> uploadCvWithDto(@PathVariable Long jobId,
                                                               @RequestParam("file") MultipartFile file,
                                                               HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        CvUploadResponseDto response = cvUploadService.uploadCvWithDto(jobId, file, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/cvs")
    public ResponseEntity<List<CvInfoResponseDto>> getUserCvsDto(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        Long userId = authorizationService.getUserIdByEmail(email);
        List<CvInfoResponseDto> cvInfos = cvUploadService.getCvInfosByUserDto(userId);
        return ResponseEntity.ok(cvInfos);
    }

    @GetMapping("/company/{companyId}/cvs")
    public ResponseEntity<List<CvInfoResponseDto>> getCompanyCvsDto(@PathVariable Long companyId) {
        List<CvInfoResponseDto> cvInfos = cvUploadService.getCvInfosByCompanyDto(companyId);
        return ResponseEntity.ok(cvInfos);
    }

    @GetMapping("/job/{jobId}/search/skills")
    public ResponseEntity<List<CvInfoResponseDto>> searchBySkillsDto(@PathVariable Long jobId,
                                                                     @RequestParam String skill) {
        List<CvInfoResponseDto> cvInfos = cvUploadService.searchCandidatesBySkillsDto(jobId, skill);
        return ResponseEntity.ok(cvInfos);
    }

    @GetMapping("/job/{jobId}/search/experience")
    public ResponseEntity<List<CvInfoResponseDto>> searchByExperienceDto(@PathVariable Long jobId,
                                                                         @RequestParam String experience) {
        List<CvInfoResponseDto> cvInfos = cvUploadService.searchCandidatesByExperienceDto(jobId, experience);
        return ResponseEntity.ok(cvInfos);
    }

    @GetMapping("/job/{jobId}/search/education")
    public ResponseEntity<List<CvInfoResponseDto>> searchByEducationDto(@PathVariable Long jobId,
                                                                        @RequestParam String education) {
        List<CvInfoResponseDto> cvInfos = cvUploadService.searchCandidatesByEducationDto(jobId, education);
        return ResponseEntity.ok(cvInfos);
    }

    @PutMapping("/{cvInfoId}/update-info")
    public ResponseEntity<Void> updateCvInfoWithDto(@PathVariable Long cvInfoId,
                                                    @Valid @RequestBody UpdateCvInfoDto updateDto) {
        cvUploadService.updateCvInfoWithDto(cvInfoId, updateDto);
        return ResponseEntity.ok().build();
    }

    // === MÉTHODES HÉRITÉES POUR COMPATIBILITÉ ===

    @PostMapping("/upload-legacy/{jobId}")
    public ResponseEntity<String> uploadCv(@PathVariable Long jobId,
                                           @RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) throws IOException {
        String email = extractEmailFromRequest(request);
        String path = cvUploadService.uploadCv(jobId, file, email);
        return ResponseEntity.ok("CV uploaded to: " + path);
    }

    @GetMapping("/job/{jobId}/candidates")
    public ResponseEntity<PaginatedResponse<CvInfoDTO>> getCandidatesForJob(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CvInfoDTO> cvInfoDTOs = cvUploadService.getCandidatesWithScoresForJobOffer(jobId, page, size);

        PaginatedResponse<CvInfoDTO> response = new PaginatedResponse<>(
                cvInfoDTOs.getContent(),
                cvInfoDTOs.getNumber(),
                cvInfoDTOs.getSize(),
                cvInfoDTOs.getTotalElements(),
                cvInfoDTOs.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/applications")
    public ResponseEntity<PaginatedResponse<JobOfferWithScoreDTO>> getUserJobApplications(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        if (userId == null) {
            String email = extractEmailFromRequest(request);
            User user = userDao.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
            userId = user.getId();
        }

        Page<JobOfferWithScoreDTO> jobOffers =
                cvUploadService.getJobOffersWithScoresForUser(userId, page, size);

        PaginatedResponse<JobOfferWithScoreDTO> response = new PaginatedResponse<>(
                jobOffers.getContent(),
                jobOffers.getNumber(),
                jobOffers.getSize(),
                jobOffers.getTotalElements(),
                jobOffers.getTotalPages()
        );

        return ResponseEntity.ok(response);
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

    @GetMapping("/user/has-applied/{jobId}")
    public ResponseEntity<Boolean> hasUserAppliedToJob(@PathVariable Long jobId,
                                                       HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        Long userId = authorizationService.getUserIdByEmail(email);
        return ResponseEntity.ok(cvUploadService.hasUserAppliedToJob(jobId, userId));
    }

    @GetMapping("/user/cvs-legacy")
    public ResponseEntity<List<CvInfo>> getUserCvs(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        Long userId = authorizationService.getUserIdByEmail(email);
        return ResponseEntity.ok(cvUploadService.getCvInfosByUser(userId));
    }

    @GetMapping("/company/{companyId}/cvs-legacy")
    public ResponseEntity<List<CvInfo>> getCompanyCvs(@PathVariable Long companyId) {
        return ResponseEntity.ok(cvUploadService.getCvInfosByCompany(companyId));
    }

    @GetMapping("/job/{jobId}/search/skills-legacy")
    public ResponseEntity<List<CvInfo>> searchBySkills(@PathVariable Long jobId,
                                                       @RequestParam String skill) {
        return ResponseEntity.ok(cvUploadService.searchCandidatesBySkills(jobId, skill));
    }

    @GetMapping("/job/{jobId}/search/experience-legacy")
    public ResponseEntity<List<CvInfo>> searchByExperience(@PathVariable Long jobId,
                                                           @RequestParam String experience) {
        return ResponseEntity.ok(cvUploadService.searchCandidatesByExperience(jobId, experience));
    }

    @GetMapping("/job/{jobId}/search/education-legacy")
    public ResponseEntity<List<CvInfo>> searchByEducation(@PathVariable Long jobId,
                                                          @RequestParam String education) {
        return ResponseEntity.ok(cvUploadService.searchCandidatesByEducation(jobId, education));
    }

    @PutMapping("/{cvInfoId}/update-info-legacy")
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