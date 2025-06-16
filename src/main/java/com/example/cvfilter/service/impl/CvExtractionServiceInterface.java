package com.example.cvfilter.service.impl;

import com.example.cvfilter.dao.entity.CvInfo;

import java.io.File;
import java.io.IOException;

public interface CvExtractionServiceInterface {
    CvInfo extractCvInfo(File cvFile, Long userId, Long jobOfferId) throws IOException;
    CvInfo extractCvInfo(File cvFile, Long userId) throws IOException;
}