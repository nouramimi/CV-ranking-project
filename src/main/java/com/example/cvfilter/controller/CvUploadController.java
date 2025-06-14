package com.example.cvfilter.controller;

import com.example.cvfilter.service.CvUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/cv")
public class CvUploadController {

    private final CvUploadService cvUploadService;

    public CvUploadController(CvUploadService cvUploadService) {
        this.cvUploadService = cvUploadService;
    }

    /*@PostMapping("/upload/{jobId}")
    public ResponseEntity<String> uploadCv(@PathVariable Long jobId, @RequestParam("file") MultipartFile file) {
        try {
            String path = cvUploadService.uploadCv(jobId, file);
            return ResponseEntity.ok("CV uploaded to: " + path);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error while uploading CV");
        }
    }*/
    @PostMapping("/upload/{jobId}")
    public ResponseEntity<String> uploadCv(@PathVariable Long jobId, @RequestParam("file") MultipartFile file) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            String path = cvUploadService.uploadCv(jobId, file, username);
            return ResponseEntity.ok("CV uploaded to: " + path);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error while uploading CV");
        }
    }
}

