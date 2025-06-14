package com.example.cvfilter.service;

import com.example.cvfilter.model.User;
import com.example.cvfilter.repository.JobOfferRepository;
import com.example.cvfilter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CvUploadService {

    private final JobOfferRepository jobOfferRepository;
    private final UserRepository userRepository;

    @Value("${cv.storage.path:data}")
    private String storagePath;

    @Value("${cv.log.file:cv_uploads.csv}")
    private String csvLogFile;

    public CvUploadService(JobOfferRepository jobOfferRepository, UserRepository userRepository) {
        this.jobOfferRepository = jobOfferRepository;
        this.userRepository = userRepository;
    }

    // Keep your original method for backward compatibility
    public String uploadCv(Long jobId, MultipartFile file) throws IOException {
        if (!jobOfferRepository.existsById(jobId)) {
            throw new IllegalArgumentException("Job offer not found");
        }

        Path jobDir = Paths.get(storagePath, String.valueOf(jobId));
        Files.createDirectories(jobDir);

        Path filePath = jobDir.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public String uploadCv(Long jobId, MultipartFile file, String username) throws IOException {
        if (!jobOfferRepository.existsById(jobId)) {
            throw new IllegalArgumentException("Job offer not found");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

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

        // Log to CSV
        logCvUpload(user.getId(), filePath.toString());

        return filePath.toString();
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



/*package com.example.cvfilter.service;

import com.example.cvfilter.repository.JobOfferRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class CvUploadService {

    private final JobOfferRepository jobOfferRepository;

    @Value("${cv.storage.path:data}")
    private String storagePath;

    public CvUploadService(JobOfferRepository jobOfferRepository) {
        this.jobOfferRepository = jobOfferRepository;
    }

    public String uploadCv(Long jobId, MultipartFile file) throws IOException {
        if (!jobOfferRepository.existsById(jobId)) {
            throw new IllegalArgumentException("Job offer not found");
        }

        Path jobDir = Paths.get(storagePath, String.valueOf(jobId));
        Files.createDirectories(jobDir);

        Path filePath = jobDir.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }
}*/

