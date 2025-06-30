package com.example.cvfilter.service;

import com.example.cvfilter.dao.CompanyDao;
import com.example.cvfilter.dao.CvInfoDao;
import com.example.cvfilter.dao.UserDao;
import com.example.cvfilter.dao.JobOfferDao;
import com.example.cvfilter.dao.entity.*;
import com.example.cvfilter.dao.repository.CvScoresRepository;
import com.example.cvfilter.dto.CvInfoDTO;
import com.example.cvfilter.dto.JobOfferWithCompanyDTO;
import com.example.cvfilter.dto.JobOfferWithScoreDTO;
import com.example.cvfilter.exception.CvUploadException;
import com.example.cvfilter.exception.JobOfferNotFoundException;
import com.example.cvfilter.exception.UserNotFoundException;
import com.example.cvfilter.service.impl.CvExtractionServiceInterface;
import com.example.cvfilter.service.impl.CvUploadServiceInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CvUploadService implements CvUploadServiceInterface {

    private final JobOfferDao jobOfferDao;
    private final UserDao userDao;
    private final CvInfoDao cvInfoDao;
    private final CompanyDao companyDao;
    private final CvExtractionServiceInterface cvExtractionService;
    private final CvScoresRepository cvScoresRepository;

    @Value("${cv.storage.path:data}")
    private String storagePath;

    @Value("${cv.log.file:cv_uploads.csv}")
    private String csvLogFile;

    public CvUploadService(JobOfferDao jobOfferDao, UserDao userDao, CvInfoDao cvInfoDao,
                           CompanyDao companyDao, CvExtractionServiceInterface cvExtractionService,
                           CvScoresRepository cvScoresRepository) {
        this.jobOfferDao = jobOfferDao;
        this.userDao = userDao;
        this.cvInfoDao = cvInfoDao;
        this.companyDao = companyDao;
        this.cvExtractionService = cvExtractionService;
        this.cvScoresRepository = cvScoresRepository;
    }

    @Override
    public String uploadCv(Long jobId, MultipartFile file) throws IOException {
        if (!jobOfferDao.existsById(jobId)) {
            throw new IllegalArgumentException("Job offer not found");
        }

        JobOffer jobOffer = jobOfferDao.findById(jobId)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found with ID: " + jobId));

        Path tempDir = Paths.get(storagePath, "temp");
        Files.createDirectories(tempDir);
        Path tempFilePath = tempDir.resolve(file.getOriginalFilename());
        file.transferTo(tempFilePath.toFile());
        File tempFile = tempFilePath.toFile();

        CvInfo cvInfo = cvExtractionService.extractAndSaveCvInfo(
                tempFile,
                null,
                jobOffer.getCompanyId(),
                jobId
        );

        Path jobDir = Paths.get(storagePath, String.valueOf(jobId));
        Files.createDirectories(jobDir);
        Path finalPath = jobDir.resolve(file.getOriginalFilename());
        Files.move(tempFilePath, finalPath, StandardCopyOption.REPLACE_EXISTING);

        cvInfo.setCvPath(finalPath.toString());
        cvInfoDao.save(cvInfo);

        return finalPath.toString();
    }

    @Override
    public String uploadCv(Long jobId, MultipartFile file, String email) {
        JobOffer jobOffer = jobOfferDao.findById(jobId)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found with ID: " + jobId));

        User user = userDao.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        try {
            Path jobDir = Paths.get(storagePath, String.valueOf(jobId));
            Files.createDirectories(jobDir);

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String baseFilename = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(0, originalFilename.lastIndexOf("."))
                    : originalFilename;

            String uniqueFilename = baseFilename + "_user_" + user.getId() + "_" +
                    System.currentTimeMillis() + extension;

            Path filePath = jobDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            CvInfo cvInfo = cvExtractionService.extractAndSaveCvInfo(
                    filePath.toFile(),
                    user.getId(),
                    jobOffer.getCompanyId(),
                    jobId
            );

            cvInfo.setCvPath(filePath.toString());

            // S'assurer que les informations utilisateur sont définies
            if (cvInfo.getName() == null || cvInfo.getName().trim().isEmpty()) {
                cvInfo.setName(user.getUsername());
            }
            if (cvInfo.getEmail() == null || cvInfo.getEmail().trim().isEmpty()) {
                cvInfo.setEmail(user.getEmail());
            }

            cvInfo = cvInfoDao.save(cvInfo);

            logCvUpload(user.getId(), filePath.toString());

            return filePath.toString();
        } catch (IOException e) {
            throw new CvUploadException("Error while uploading CV", e);
        }
    }

    @Override
    public Page<CvInfo> getCandidatesForJobOffer(Long jobOfferId, int page, int size) {
        if (!jobOfferDao.existsById(jobOfferId)) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return cvInfoDao.findByJobOfferIdWithAllFields(jobOfferId, pageable);
    }

    public Page<CvInfoDTO> getCandidatesWithScoresForJobOffer(Long jobOfferId, int page, int size) {
        if (!jobOfferDao.existsById(jobOfferId)) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }

        // Récupérer TOUS les candidats pour pouvoir trier correctement
        List<CvInfo> allCvInfos = cvInfoDao.findByJobOfferId(jobOfferId);

        List<CvInfoDTO> allDtos = allCvInfos.stream()
                .map(cvInfo -> {
                    Optional<CvScores> cvScores = cvScoresRepository
                            .findByUserIdAndJobOfferId(cvInfo.getUserId(), jobOfferId);

                    if (cvScores.isPresent()) {
                        return new CvInfoDTO(cvInfo, cvScores.get());
                    } else {
                        return new CvInfoDTO(cvInfo);
                    }
                })
                // Trier par finalScore en ordre décroissant
                .sorted((dto1, dto2) -> {
                    Double score1 = dto1.getCvScores() != null ?
                            dto1.getCvScores().getFinalScore().doubleValue() : 0.0;
                    Double score2 = dto2.getCvScores() != null ?
                            dto2.getCvScores().getFinalScore().doubleValue() : 0.0;
                    return Double.compare(score2, score1); // Ordre décroissant
                })
                .collect(Collectors.toList());

        // Appliquer la pagination manuellement après le tri
        int totalElements = allDtos.size();
        int start = page * size;
        int end = Math.min(start + size, totalElements);

        List<CvInfoDTO> pageContent = start < totalElements ?
                allDtos.subList(start, end) :
                new ArrayList<>();

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(pageContent, pageable, totalElements);
    }

    // Tous vos autres méthodes restent identiques
    public List<CvInfo> getUniqueCandidatesForJobOffer(Long jobOfferId) {
        if (!jobOfferDao.existsById(jobOfferId)) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }

        List<Long> userIds = cvInfoDao.findDistinctUserIdsByJobOfferId(jobOfferId);
        return userIds.stream()
                .map(userId -> cvInfoDao.findLatestByJobOfferIdAndUserId(jobOfferId, userId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public long getCandidateCountForJobOffer(Long jobOfferId) {
        if (!jobOfferDao.existsById(jobOfferId)) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }
        return cvInfoDao.countDistinctUsersByJobOfferId(jobOfferId);
    }

    public List<CvInfo> getCvInfosForJobOffer(Long jobOfferId) {
        if (!jobOfferDao.existsById(jobOfferId)) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }
        return cvInfoDao.findByJobOfferId(jobOfferId);
    }

    public List<CvInfo> getCandidatesWithExtractedInfo(Long jobOfferId) {
        if (!jobOfferDao.existsById(jobOfferId)) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }
        return cvInfoDao.findByJobOfferIdWithExtractedInfo(jobOfferId);
    }

    public List<Long> getJobOffersForUser(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        return cvInfoDao.findDistinctJobOfferIdsByUserId(userId);
    }

    public boolean hasUserAppliedToJob(Long jobOfferId, Long userId) {
        return cvInfoDao.existsByJobOfferIdAndUserId(jobOfferId, userId);
    }

    public List<CvInfo> getCvInfosByUser(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        return cvInfoDao.findByUserId(userId);
    }

    public List<CvInfo> getCvInfosByCompany(Long companyId) {
        return cvInfoDao.findByCompanyId(companyId);
    }

    public List<CvInfo> searchCandidatesBySkills(Long jobOfferId, String skill) {
        if (!jobOfferDao.existsById(jobOfferId)) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }
        return cvInfoDao.findByJobOfferIdAndSkillsContaining(jobOfferId, skill);
    }

    public List<CvInfo> searchCandidatesByExperience(Long jobOfferId, String experience) {
        if (!jobOfferDao.existsById(jobOfferId)) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }
        return cvInfoDao.findByJobOfferIdAndExperienceContaining(jobOfferId, experience);
    }

    public List<CvInfo> searchCandidatesByEducation(Long jobOfferId, String education) {
        if (!jobOfferDao.existsById(jobOfferId)) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }
        return cvInfoDao.findByJobOfferIdAndEducationContaining(jobOfferId, education);
    }

    public void updateCvInfo(Long cvInfoId, String name, String email, String phone,
                             String description, String skills, String experience, String education) {
        CvInfo cvInfo = cvInfoDao.findById(cvInfoId)
                .orElseThrow(() -> new IllegalArgumentException("CvInfo not found with ID: " + cvInfoId));

        cvInfo.setName(name);
        cvInfo.setEmail(email);
        cvInfo.setPhone(phone);
        cvInfo.setDescription(description);
        cvInfo.setSkills(skills);
        cvInfo.setExperience(experience);
        cvInfo.setEducation(education);

        cvInfoDao.save(cvInfo);
    }

    private void logCvUpload(Long userId, String cvPath) throws IOException {
        Path csvPath = Paths.get(csvLogFile);
        boolean fileExists = Files.exists(csvPath);

        try (FileWriter writer = new FileWriter(csvLogFile, true)) {
            if (!fileExists) {
                writer.append("user_id,cv_path,upload_timestamp\n");
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String escapedPath = cvPath.replace("\"", "\"\"");

            writer.append(String.format("%d,\"%s\",%s\n", userId, escapedPath, timestamp));
        }
    }

    @Override
    public Page<JobOfferWithCompanyDTO> getJobOffersWithCompanyDetailsForUser(Long userId, int page, int size) {
        if (!userDao.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Long> jobOfferIdsPage = cvInfoDao.findDistinctJobOfferIdsByUserId(userId, pageable);

        List<JobOfferWithCompanyDTO> content = jobOfferIdsPage.getContent().stream()
                .map(jobOfferId -> {
                    JobOffer jobOffer = jobOfferDao.findById(jobOfferId)
                            .orElseThrow(() -> new JobOfferNotFoundException(
                                    "Job offer not found with ID: " + jobOfferId));

                    Company company = companyDao.findById(jobOffer.getCompanyId())
                            .orElse(null);

                    return new JobOfferWithCompanyDTO(jobOffer, company);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, jobOfferIdsPage.getTotalElements());
    }

    public Page<JobOfferWithScoreDTO> getJobOffersWithScoresForUser(Long userId, int page, int size) {
        if (!userDao.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Long> jobOfferIdsPage = cvInfoDao.findDistinctJobOfferIdsByUserId(userId, pageable);

        List<JobOfferWithScoreDTO> content = jobOfferIdsPage.getContent().stream()
                .map(jobOfferId -> {

                    JobOffer jobOffer = jobOfferDao.findById(jobOfferId)
                            .orElseThrow(() -> new JobOfferNotFoundException(
                                    "Job offer not found with ID: " + jobOfferId));

                    Company company = companyDao.findById(jobOffer.getCompanyId())
                            .orElse(null);

                    Optional<CvScores> cvScores = cvScoresRepository
                            .findByUserIdAndJobOfferId(userId, jobOfferId);

                    if (cvScores.isPresent()) {
                        return new JobOfferWithScoreDTO(jobOffer, company, cvScores.get());
                    } else {
                        return new JobOfferWithScoreDTO(jobOffer, company);
                    }
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, jobOfferIdsPage.getTotalElements());
    }
}