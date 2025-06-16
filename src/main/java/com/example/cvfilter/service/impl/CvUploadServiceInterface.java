package com.example.cvfilter.service.impl;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CvUploadServiceInterface {
    String uploadCv(Long jobId, MultipartFile file) throws IOException;
    String uploadCv(Long jobId, MultipartFile file, String username) throws IOException;
}
