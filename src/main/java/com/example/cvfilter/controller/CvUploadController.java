package com.example.cvfilter.controller;

import com.example.cvfilter.config.JwtUtils;
import com.example.cvfilter.service.impl.CvUploadServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/cv")
public class CvUploadController {

    private final CvUploadServiceInterface cvUploadService;
    private final JwtUtils jwtUtils;

    public CvUploadController(CvUploadServiceInterface cvUploadService, JwtUtils jwtUtils) {
        this.cvUploadService = cvUploadService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/upload/{jobId}")
    public ResponseEntity<String> uploadCv(@PathVariable Long jobId,
                                           @RequestParam("file") MultipartFile file,
                                           HttpServletRequest request) throws IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String username = jwtUtils.extractUsername(token);

        if (username == null) {
            throw new IllegalArgumentException("Unable to extract username from token");
        }

        String path = cvUploadService.uploadCv(jobId, file, username);
        return ResponseEntity.ok("CV uploaded to: " + path);
    }
}
