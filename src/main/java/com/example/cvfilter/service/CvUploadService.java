package com.example.cvfilter.service;

import com.example.cvfilter.dao.CvInfoDao;
import com.example.cvfilter.dao.UserDao;
import com.example.cvfilter.dao.JobOfferDao;
import com.example.cvfilter.dao.entity.CvInfo;
import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.dao.entity.User;
import com.example.cvfilter.exception.CvUploadException;
import com.example.cvfilter.exception.JobOfferNotFoundException;
import com.example.cvfilter.exception.UserNotFoundException;
import com.example.cvfilter.service.impl.CvUploadServiceInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CvUploadService implements CvUploadServiceInterface {

    private final JobOfferDao jobOfferDao;
    private final UserDao userDao;
    private final CvInfoDao cvInfoDao;

    @Value("${cv.storage.path:data}")
    private String storagePath;

    @Value("${cv.log.file:cv_uploads.csv}")
    private String csvLogFile;

    public CvUploadService(JobOfferDao jobOfferDao, UserDao userDao, CvInfoDao cvInfoDao) {
        this.jobOfferDao = jobOfferDao;
        this.userDao = userDao;
        this.cvInfoDao = cvInfoDao;
    }

    @Override
    public String uploadCv(Long jobId, MultipartFile file) throws IOException {
        if (!jobOfferDao.existsById(jobId)) {
            throw new IllegalArgumentException("Job offer not found");
        }

        Path jobDir = Paths.get(storagePath, String.valueOf(jobId));
        Files.createDirectories(jobDir);

        Path filePath = jobDir.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    @Override
    public String uploadCv(Long jobId, MultipartFile file, String email) { // Changed parameter
        JobOffer jobOffer = jobOfferDao.findById(jobId)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found with ID: " + jobId));

        User user = userDao.findByEmail(email) // Changed to findByEmail
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

            // Créer un nouvel enregistrement CvInfo
            CvInfo cvInfo = new CvInfo(user.getId(), jobId, jobOffer.getCompanyId(), filePath.toString());
            cvInfoDao.save(cvInfo);

            // Log pour compatibilité avec l'ancien système
            logCvUpload(user.getId(), filePath.toString());

            return filePath.toString();
        } catch (IOException e) {
            throw new CvUploadException("Error while uploading CV", e);
        }
    }

    // Nouvelles méthodes pour obtenir les candidats via CvInfo
    public List<CvInfo> getCandidatesForJobOffer(Long jobOfferId) {
        if (!jobOfferDao.existsById(jobOfferId)) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }
        return cvInfoDao.findByJobOfferIdWithAllFields(jobOfferId);
    }

    public List<CvInfo> getUniqueCandidatesForJobOffer(Long jobOfferId) {
        if (!jobOfferDao.existsById(jobOfferId)) {
            throw new JobOfferNotFoundException("Job offer not found with ID: " + jobOfferId);
        }

        // Obtenir les candidats uniques (le plus récent CV par utilisateur)
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

    // Méthodes de recherche avancée
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

    // Méthode pour mettre à jour les informations extraites d'un CV
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
}