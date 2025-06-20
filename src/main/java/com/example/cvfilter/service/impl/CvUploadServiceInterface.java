package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.CvInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CvUploadServiceInterface {
    String uploadCv(Long jobId, MultipartFile file) throws IOException;
    String uploadCv(Long jobId, MultipartFile file, String email) throws IOException;
    List<CvInfo> getCandidatesForJobOffer(Long jobOfferId);
    List<CvInfo> getUniqueCandidatesForJobOffer(Long jobOfferId);
    long getCandidateCountForJobOffer(Long jobOfferId);
    List<CvInfo> getCvInfosForJobOffer(Long jobOfferId);
    List<CvInfo> getCandidatesWithExtractedInfo(Long jobOfferId);
    List<Long> getJobOffersForUser(Long userId);
    boolean hasUserAppliedToJob(Long jobOfferId, Long userId);
    List<CvInfo> getCvInfosByUser(Long userId);
    List<CvInfo> getCvInfosByCompany(Long companyId);
    List<CvInfo> searchCandidatesBySkills(Long jobOfferId, String skill);
    List<CvInfo> searchCandidatesByExperience(Long jobOfferId, String experience);
    List<CvInfo> searchCandidatesByEducation(Long jobOfferId, String education);
    void updateCvInfo(Long cvInfoId, String name, String email, String phone,
                      String description, String skills, String experience, String education);
}
