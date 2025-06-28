package com.example.cvfilter.service;

import com.example.cvfilter.dao.entity.CvScores;
import com.example.cvfilter.dao.repository.CvScoresRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class CvNormalizationPipelineService {

    private static final Logger logger = LoggerFactory.getLogger(CvNormalizationPipelineService.class);

    @Autowired
    private CvScoresRepository cvScoresRepository;

    @Value("${cv.extracted.info.file:cv_extracted_info.csv}")
    private String csvFilePath;

    @Value("${python.executable:python}")
    private String pythonExecutable;

    @Value("${python.script1.path:python/cv_processor_spring.py}")
    private String script1Path;

    @Value("${python.script2.path:python/cv_organization_scorer_spring.py}")
    private String script2Path;

    @Value("${python.script3.path:python/cv_job_matcher_spring.py}")
    private String script3Path;

    @Value("${python.working.directory:.}")
    private String workingDirectory;

    @Scheduled(fixedRate = 60000)
    public void checkAndProcessCVs() {
        try {
            logger.info("🔍 Checking for CVs that need normalization and scoring...");

            List<CvRecord> allCVs = readCvExtractedInfo();

            List<CvRecord> unprocessedCVs = filterUnprocessedCVs(allCVs);

            if (unprocessedCVs.isEmpty()) {
                logger.debug("No CVs need processing");
                return;
            }

            logger.info("Found {} CVs that need processing", unprocessedCVs.size());

            for (CvRecord cv : unprocessedCVs) {
                try {
                    processCvPipeline(cv);
                } catch (Exception e) {
                    logger.error("Failed to process CV: user={}, job={}", cv.getUserId(), cv.getJobOfferId(), e);
                }
            }

        } catch (Exception e) {
            logger.error("Error in CV normalization pipeline", e);
        }
    }

    private List<CvRecord> readCvExtractedInfo() {
        List<CvRecord> cvList = new ArrayList<>();

        File csvFile = new File(csvFilePath);
        if (!csvFile.exists()) {
            logger.warn("CSV file not found: {}", csvFilePath);
            return cvList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                CvRecord cv = parseCvRecord(line);
                if (cv != null) {
                    cvList.add(cv);
                }
            }

            logger.info("Read {} CVs from extracted info file", cvList.size());

        } catch (IOException e) {
            logger.error("Error reading CV extracted info file", e);
        }

        return cvList;
    }

    private CvRecord parseCvRecord(String line) {
        try {
            String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            if (values.length >= 11) {
                CvRecord cv = new CvRecord();
                cv.setUserId(Long.parseLong(values[0].trim()));
                cv.setJobOfferId(Long.parseLong(values[1].trim()));
                cv.setCvPath(values[2].replace("\"", "").trim());
                cv.setName(values[3].replace("\"", "").trim());
                cv.setEmail(values[4].replace("\"", "").trim());
                cv.setPhone(values[5].replace("\"", "").trim());
                cv.setDescription(values[6].replace("\"", "").trim());
                cv.setSkills(values[7].replace("\"", "").trim());
                cv.setExperience(values[8].replace("\"", "").trim());
                cv.setEducation(values[9].replace("\"", "").trim());
                cv.setExtractedAt(values[10].replace("\"", "").trim());

                return cv;
            }
        } catch (Exception e) {
            logger.warn("Could not parse CV record: {}", line, e);
        }

        return null;
    }

    private List<CvRecord> filterUnprocessedCVs(List<CvRecord> allCVs) {
        List<CvRecord> unprocessed = new ArrayList<>();

        for (CvRecord cv : allCVs) {
            if (!hasScores(cv)) {
                unprocessed.add(cv);
            }
        }

        return unprocessed;
    }

    private boolean hasScores(CvRecord cv) {
        return cvScoresRepository.existsByUserIdAndJobOfferId(cv.getUserId(), cv.getJobOfferId());
    }

    private void processCvPipeline(CvRecord cv) throws Exception {
        logger.info("🚀 Starting pipeline for CV: user={}, job={}", cv.getUserId(), cv.getJobOfferId());

        String tempDir = System.getProperty("java.io.tmpdir");
        String tempCsvInput = tempDir + "/temp_cv_input_" + cv.getUserId() + "_" + cv.getJobOfferId() + ".csv";
        String script1Output = tempDir + "/script1_output_" + cv.getUserId() + "_" + cv.getJobOfferId() + ".csv";
        String script2Output = tempDir + "/script2_output_" + cv.getUserId() + "_" + cv.getJobOfferId() + ".json";
        String script3Output = tempDir + "/script3_output_" + cv.getUserId() + "_" + cv.getJobOfferId() + ".json";

        try {
            logger.info("📄 Creating temporary CSV for user={}, job={}", cv.getUserId(), cv.getJobOfferId());
            createTempCsvForCv(cv, tempCsvInput);

            logger.info("📝 Running normalization script for user={}, job={}", cv.getUserId(), cv.getJobOfferId());
            boolean script1Success = runPythonScriptWithTimeout(script1Path, tempCsvInput, script1Output, 2);

            if (!script1Success) {
                throw new RuntimeException("Script 1 (normalization) failed");
            }

            logger.info("📊 Running scoring script for user={}, job={}", cv.getUserId(), cv.getJobOfferId());
            boolean script2Success = runPythonScriptWithTimeout(script2Path, script1Output, script2Output, 2);

            if (!script2Success) {
                throw new RuntimeException("Script 2 (scoring) failed");
            }

            logger.info("🎯 Running job matching script for user={}, job={}", cv.getUserId(), cv.getJobOfferId());
            boolean script3Success = runJobMatchingScript(script1Output, script3Output);

            if (!script3Success) {
                throw new RuntimeException("Script 3 (job matching) failed");
            }

            CvScoreResult organizationResults = readScoreResults(script2Output);
            CvJobMatchResult jobMatchResults = readJobMatchResults(script3Output);

            CombinedCvResult combinedResults = combineResults(organizationResults, jobMatchResults);

            saveResults(cv, combinedResults);

            logger.info("✅ Pipeline completed successfully for user={}, job={}, final_score={}",
                    cv.getUserId(), cv.getJobOfferId(), combinedResults.getFinalScore());

        } finally {
            cleanupTempFiles(tempCsvInput, script1Output, script2Output, script3Output);
        }
    }

    private void createTempCsvForCv(CvRecord cv, String outputPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("user_id,job_offer_id,cv_path,name,email,phone,description,skills,experience,education,extracted_at");

            writer.printf("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    cv.getUserId(),
                    cv.getJobOfferId(),
                    escapeCsvField(cv.getCvPath()),
                    escapeCsvField(cv.getName()),
                    escapeCsvField(cv.getEmail()),
                    escapeCsvField(cv.getPhone()),
                    escapeCsvField(cv.getDescription()),
                    escapeCsvField(cv.getSkills()),
                    escapeCsvField(cv.getExperience()),
                    escapeCsvField(cv.getEducation()),
                    escapeCsvField(cv.getExtractedAt())
            );
        }

        logger.info("Created temporary CSV: {}", outputPath);
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        return field.replace("\"", "\"\"");
    }

    private boolean runPythonScriptWithTimeout(String scriptPath, String inputFile, String outputFile, int timeoutMinutes) {
        try {
            List<String> command = Arrays.asList(
                    pythonExecutable,
                    scriptPath,
                    "--input", inputFile,
                    "--output", outputFile
            );

            logger.info("Executing: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(workingDirectory));

            Process process = processBuilder.start();

            boolean finished = process.waitFor(timeoutMinutes, TimeUnit.MINUTES);

            if (!finished) {
                logger.error("Script timeout after {} minutes - killing process", timeoutMinutes);
                process.destroyForcibly();
                return false;
            }

            String output = readProcessOutput(process.getInputStream());
            String errorOutput = readProcessOutput(process.getErrorStream());

            int exitCode = process.exitValue();

            if (exitCode == 0) {
                logger.info("Script executed successfully");
                if (!output.trim().isEmpty()) {
                    logger.debug("Script output: {}", output);
                }
                return true;
            } else {
                logger.error("Script failed with exit code: {}", exitCode);
                logger.error("Error output: {}", errorOutput);
                return false;
            }

        } catch (Exception e) {
            logger.error("Exception running script: {}", scriptPath, e);
            return false;
        }
    }

    private boolean runJobMatchingScript(String inputFile, String outputFile) {
        try {
            List<String> command = Arrays.asList(
                    pythonExecutable,
                    script3Path,
                    "--input", inputFile,
                    "--output", outputFile,
                    "--db-host", "localhost",
                    "--db-name", "cv_filter",
                    "--db-user", "postgres",
                    "--db-password", "2003"
            );

            logger.info("Executing Job Matching: {}", String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(workingDirectory));

            Process process = processBuilder.start();

            boolean finished = process.waitFor(3, TimeUnit.MINUTES);

            if (!finished) {
                logger.error("Job matching script timeout - killing process");
                process.destroyForcibly();
                return false;
            }

            String output = readProcessOutput(process.getInputStream());
            String errorOutput = readProcessOutput(process.getErrorStream());

            int exitCode = process.exitValue();

            if (exitCode == 0) {
                logger.info("Job matching executed successfully");
                if (!output.trim().isEmpty()) {
                    logger.debug("Job matching output: {}", output);
                }
                return true;
            } else {
                logger.error("Job matching failed with exit code: {}", exitCode);
                logger.error("Error output: {}", errorOutput);
                return false;
            }

        } catch (Exception e) {
            logger.error("Exception running job matching script", e);
            return false;
        }
    }

    private String readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().reduce("", (a, b) -> a + "\n" + b);
        }
    }

    private CvScoreResult readScoreResults(String outputFile) throws IOException {
        String content = Files.readString(Paths.get(outputFile));

        CvScoreResult result = new CvScoreResult();

        try {
            if (content.contains("organization_score")) {
                String scoreStr = content.split("\"organization_score\":\\s*")[1].split("[,}]")[0];
                result.setOrganizationScore(Double.parseDouble(scoreStr.trim()));
            }

            if (content.contains("technical_score")) {
                String scoreStr = content.split("\"technical_score\":\\s*")[1].split("[,}]")[0];
                result.setTechnicalScore(Double.parseDouble(scoreStr.trim()));
            }

            if (content.contains("composite_score")) {
                String scoreStr = content.split("\"composite_score\":\\s*")[1].split("[,}]")[0];
                result.setCompositeScore(Double.parseDouble(scoreStr.trim()));
            }

            if (content.contains("experience_score")) {
                String scoreStr = content.split("\"experience_score\":\\s*")[1].split("[,}]")[0];
                result.setExperienceScore(Double.parseDouble(scoreStr.trim()));
            }

            if (content.contains("skills_score")) {
                String scoreStr = content.split("\"skills_score\":\\s*")[1].split("[,}]")[0];
                result.setSkillsScore(Double.parseDouble(scoreStr.trim()));
            }

            if (content.contains("education_score")) {
                String scoreStr = content.split("\"education_score\":\\s*")[1].split("[,}]")[0];
                result.setEducationScore(Double.parseDouble(scoreStr.trim()));
            }

        } catch (Exception e) {
            logger.warn("Error parsing specific scores from JSON, using defaults: {}", e.getMessage());
        }

        result.setProcessedAt(LocalDateTime.now());
        return result;
    }

    private CvJobMatchResult readJobMatchResults(String outputFile) throws IOException {
        try {
            String content = Files.readString(Paths.get(outputFile));

            CvJobMatchResult result = new CvJobMatchResult();

            try {

                if (content.contains("overall_match_score")) {
                    String scoreStr = content.split("\"overall_match_score\":\\s*")[1].split("[,}]")[0];
                    result.setOverallMatchScore(Double.parseDouble(scoreStr.trim()));
                }

                if (content.contains("skills_match_score")) {
                    String scoreStr = extractNestedScore(content, "skills_match", "skills_match_score");
                    if (scoreStr != null) {
                        result.setSkillsMatchScore(Double.parseDouble(scoreStr));
                    }
                }

                if (content.contains("experience_match_score")) {
                    String scoreStr = extractNestedScore(content, "experience_match", "experience_match_score");
                    if (scoreStr != null) {
                        result.setExperienceMatchScore(Double.parseDouble(scoreStr));
                    }
                }

                if (content.contains("education_match_score")) {
                    String scoreStr = extractNestedScore(content, "education_match", "education_match_score");
                    if (scoreStr != null) {
                        result.setEducationMatchScore(Double.parseDouble(scoreStr));
                    }
                }

                if (content.contains("content_relevance_score")) {
                    String scoreStr = extractNestedScore(content, "content_relevance", "content_relevance_score");
                    if (scoreStr != null) {
                        result.setContentRelevanceScore(Double.parseDouble(scoreStr));
                    }
                }

                if (content.contains("match_level")) {
                    String levelStr = content.split("\"match_level\":\\s*\"")[1].split("\"")[0];
                    result.setMatchLevel(levelStr.trim());
                }

                if (content.contains("job_title")) {
                    String titleStr = content.split("\"job_title\":\\s*\"")[1].split("\"")[0];
                    result.setJobTitle(titleStr.trim());
                }

            } catch (Exception e) {
                logger.warn("Error parsing job match scores from JSON, using defaults: {}", e.getMessage());
            }

            result.setProcessedAt(LocalDateTime.now());
            return result;

        } catch (Exception e) {
            logger.error("Error reading job match results from file: {}", outputFile, e);

            CvJobMatchResult defaultResult = new CvJobMatchResult();
            defaultResult.setOverallMatchScore(50.0);
            defaultResult.setMatchLevel("UNKNOWN");
            defaultResult.setProcessedAt(LocalDateTime.now());
            return defaultResult;
        }
    }

    private String extractNestedScore(String content, String section, String scoreField) {
        try {

            String sectionPattern = "\"" + section + "\":\\s*\\{";
            int sectionStart = content.indexOf(sectionPattern);
            if (sectionStart == -1) return null;

            int braceCount = 0;
            int searchStart = sectionStart + sectionPattern.length() - 1;
            int sectionEnd = searchStart;

            for (int i = searchStart; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '{') braceCount++;
                else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        sectionEnd = i;
                        break;
                    }
                }
            }

            String sectionContent = content.substring(searchStart, sectionEnd + 1);

            String scorePattern = "\"" + scoreField + "\":\\s*";
            int scoreStart = sectionContent.indexOf(scorePattern);
            if (scoreStart == -1) return null;

            scoreStart += scorePattern.length();
            int scoreEnd = scoreStart;

            while (scoreEnd < sectionContent.length() &&
                    (Character.isDigit(sectionContent.charAt(scoreEnd)) ||
                            sectionContent.charAt(scoreEnd) == '.')) {
                scoreEnd++;
            }

            return sectionContent.substring(scoreStart, scoreEnd);

        } catch (Exception e) {
            logger.warn("Error extracting nested score {}.{}: {}", section, scoreField, e.getMessage());
            return null;
        }
    }

    private CombinedCvResult combineResults(CvScoreResult organizationResults, CvJobMatchResult jobMatchResults) {
        CombinedCvResult combined = new CombinedCvResult();

        combined.setOrganizationScore(organizationResults.getOrganizationScore() != null ?
                organizationResults.getOrganizationScore() : 50.0);
        combined.setTechnicalScore(organizationResults.getTechnicalScore() != null ?
                organizationResults.getTechnicalScore() : 50.0);
        combined.setCompositeScore(organizationResults.getCompositeScore() != null ?
                organizationResults.getCompositeScore() : 50.0);
        combined.setExperienceScore(organizationResults.getExperienceScore() != null ?
                organizationResults.getExperienceScore() : 50.0);
        combined.setSkillsScore(organizationResults.getSkillsScore() != null ?
                organizationResults.getSkillsScore() : 50.0);
        combined.setEducationScore(organizationResults.getEducationScore() != null ?
                organizationResults.getEducationScore() : 50.0);

        combined.setJobMatchScore(jobMatchResults.getOverallMatchScore() != null ?
                jobMatchResults.getOverallMatchScore() : 50.0);
        combined.setSkillsMatchScore(jobMatchResults.getSkillsMatchScore() != null ?
                jobMatchResults.getSkillsMatchScore() : 50.0);
        combined.setExperienceMatchScore(jobMatchResults.getExperienceMatchScore() != null ?
                jobMatchResults.getExperienceMatchScore() : 50.0);
        combined.setEducationMatchScore(jobMatchResults.getEducationMatchScore() != null ?
                jobMatchResults.getEducationMatchScore() : 50.0);
        combined.setContentRelevanceScore(jobMatchResults.getContentRelevanceScore() != null ?
                jobMatchResults.getContentRelevanceScore() : 50.0);
        combined.setMatchLevel(jobMatchResults.getMatchLevel() != null ?
                jobMatchResults.getMatchLevel() : "UNKNOWN");
        combined.setJobTitle(jobMatchResults.getJobTitle() != null ?
                jobMatchResults.getJobTitle() : "Unknown Job");

        double compositeScore = combined.getCompositeScore() != null ? combined.getCompositeScore() : 50.0;
        double jobMatchScore = combined.getJobMatchScore() != null ? combined.getJobMatchScore() : 50.0;

        double finalScore = (compositeScore * 0.40) + (jobMatchScore * 0.60);

        combined.setFinalScore(finalScore);
        combined.setProcessedAt(LocalDateTime.now());

        logger.debug("Combined results: composite={}, jobMatch={}, final={}",
                compositeScore, jobMatchScore, finalScore);

        return combined;
    }

    @Transactional
    private void saveResults(CvRecord cv, CombinedCvResult results) {
        try {
            logger.info("💾 Saving combined scores to database for user={}, job={}", cv.getUserId(), cv.getJobOfferId());

            Optional<CvScores> existingScore = cvScoresRepository.findByUserIdAndJobOfferId(
                    cv.getUserId(), cv.getJobOfferId());

            CvScores cvScore;
            if (existingScore.isPresent()) {
                cvScore = existingScore.get();
                logger.info("Updating existing score for user={}, job={}", cv.getUserId(), cv.getJobOfferId());
            } else {
                cvScore = new CvScores(cv.getUserId(), cv.getJobOfferId(), cv.getCvPath());
                logger.info("Creating new score for user={}, job={}", cv.getUserId(), cv.getJobOfferId());
            }

            cvScore.setOrganizationScore(results.getOrganizationScore());
            cvScore.setTechnicalScore(results.getTechnicalScore());
            cvScore.setCompositeScore(results.getCompositeScore());
            cvScore.setExperienceScore(results.getExperienceScore());
            cvScore.setSkillsScore(results.getSkillsScore());
            cvScore.setEducationScore(results.getEducationScore());

            cvScore.setJobMatchScore(results.getJobMatchScore());
            cvScore.setSkillsMatchScore(results.getSkillsMatchScore());
            cvScore.setExperienceMatchScore(results.getExperienceMatchScore());
            cvScore.setEducationMatchScore(results.getEducationMatchScore());
            cvScore.setContentRelevanceScore(results.getContentRelevanceScore());
            cvScore.setMatchLevel(results.getMatchLevel());
            cvScore.setJobTitle(results.getJobTitle());

            cvScore.setFinalScore(results.getFinalScore());
            cvScore.setProcessedAt(results.getProcessedAt());

            CvScores savedScore = cvScoresRepository.save(cvScore);

            logger.info("✅ Combined scores saved to database with ID: {}", savedScore.getId());
            logger.info("   - Organization Score: {}/100", results.getOrganizationScore());
            logger.info("   - Job Match Score: {}/100", results.getJobMatchScore());
            logger.info("   - Final Score: {}/100", results.getFinalScore());
            logger.info("   - Match Level: {}", results.getMatchLevel());

        } catch (Exception e) {
            logger.error("❌ Error saving combined scores to database for user={}, job={}",
                    cv.getUserId(), cv.getJobOfferId(), e);
            throw new RuntimeException("Failed to save CV scores", e);
        }
    }

    private void cleanupTempFiles(String... files) {
        for (String file : files) {
            try {
                Files.deleteIfExists(Paths.get(file));
            } catch (IOException e) {
                logger.warn("Could not delete temp file: {}", file);
            }
        }
    }

    public static class CvRecord {
        private Long userId;
        private Long jobOfferId;
        private String cvPath;
        private String name;
        private String email;
        private String phone;
        private String description;
        private String skills;
        private String experience;
        private String education;
        private String extractedAt;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Long getJobOfferId() { return jobOfferId; }
        public void setJobOfferId(Long jobOfferId) { this.jobOfferId = jobOfferId; }

        public String getCvPath() { return cvPath; }
        public void setCvPath(String cvPath) { this.cvPath = cvPath; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getSkills() { return skills; }
        public void setSkills(String skills) { this.skills = skills; }

        public String getExperience() { return experience; }
        public void setExperience(String experience) { this.experience = experience; }

        public String getEducation() { return education; }
        public void setEducation(String education) { this.education = education; }

        public String getExtractedAt() { return extractedAt; }
        public void setExtractedAt(String extractedAt) { this.extractedAt = extractedAt; }
    }

    public static class CvScoreResult {
        private Double organizationScore;
        private Double technicalScore;
        private Double compositeScore;
        private Double experienceScore;
        private Double skillsScore;
        private Double educationScore;
        private LocalDateTime processedAt;

        public Double getOrganizationScore() { return organizationScore; }
        public void setOrganizationScore(Double organizationScore) { this.organizationScore = organizationScore; }

        public Double getTechnicalScore() { return technicalScore; }
        public void setTechnicalScore(Double technicalScore) { this.technicalScore = technicalScore; }

        public Double getCompositeScore() { return compositeScore; }
        public void setCompositeScore(Double compositeScore) { this.compositeScore = compositeScore; }

        public Double getExperienceScore() { return experienceScore; }
        public void setExperienceScore(Double experienceScore) { this.experienceScore = experienceScore; }

        public Double getSkillsScore() { return skillsScore; }
        public void setSkillsScore(Double skillsScore) { this.skillsScore = skillsScore; }

        public Double getEducationScore() { return educationScore; }
        public void setEducationScore(Double educationScore) { this.educationScore = educationScore; }

        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    }

    public static class CvJobMatchResult {
        private Double overallMatchScore;
        private Double skillsMatchScore;
        private Double experienceMatchScore;
        private Double educationMatchScore;
        private Double contentRelevanceScore;
        private String matchLevel;
        private String jobTitle;
        private LocalDateTime processedAt;

        public CvJobMatchResult() {}

        public Double getOverallMatchScore() { return overallMatchScore; }
        public void setOverallMatchScore(Double overallMatchScore) { this.overallMatchScore = overallMatchScore; }

        public Double getSkillsMatchScore() { return skillsMatchScore; }
        public void setSkillsMatchScore(Double skillsMatchScore) { this.skillsMatchScore = skillsMatchScore; }

        public Double getExperienceMatchScore() { return experienceMatchScore; }
        public void setExperienceMatchScore(Double experienceMatchScore) { this.experienceMatchScore = experienceMatchScore; }

        public Double getEducationMatchScore() { return educationMatchScore; }
        public void setEducationMatchScore(Double educationMatchScore) { this.educationMatchScore = educationMatchScore; }

        public Double getContentRelevanceScore() { return contentRelevanceScore; }
        public void setContentRelevanceScore(Double contentRelevanceScore) { this.contentRelevanceScore = contentRelevanceScore; }

        public String getMatchLevel() { return matchLevel; }
        public void setMatchLevel(String matchLevel) { this.matchLevel = matchLevel; }

        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    }

    public static class CombinedCvResult {
        private Double organizationScore;
        private Double technicalScore;
        private Double compositeScore;
        private Double experienceScore;
        private Double skillsScore;
        private Double educationScore;

        private Double jobMatchScore;
        private Double skillsMatchScore;
        private Double experienceMatchScore;
        private Double educationMatchScore;
        private Double contentRelevanceScore;
        private String matchLevel;
        private String jobTitle;

        private Double finalScore;
        private LocalDateTime processedAt;

        public CombinedCvResult() {}

        public Double getOrganizationScore() { return organizationScore; }
        public void setOrganizationScore(Double organizationScore) { this.organizationScore = organizationScore; }

        public Double getTechnicalScore() { return technicalScore; }
        public void setTechnicalScore(Double technicalScore) { this.technicalScore = technicalScore; }

        public Double getCompositeScore() { return compositeScore; }
        public void setCompositeScore(Double compositeScore) { this.compositeScore = compositeScore; }

        public Double getExperienceScore() { return experienceScore; }
        public void setExperienceScore(Double experienceScore) { this.experienceScore = experienceScore; }

        public Double getSkillsScore() { return skillsScore; }
        public void setSkillsScore(Double skillsScore) { this.skillsScore = skillsScore; }

        public Double getEducationScore() { return educationScore; }
        public void setEducationScore(Double educationScore) { this.educationScore = educationScore; }

        public Double getJobMatchScore() { return jobMatchScore; }
        public void setJobMatchScore(Double jobMatchScore) { this.jobMatchScore = jobMatchScore; }

        public Double getSkillsMatchScore() { return skillsMatchScore; }
        public void setSkillsMatchScore(Double skillsMatchScore) { this.skillsMatchScore = skillsMatchScore; }

        public Double getExperienceMatchScore() { return experienceMatchScore; }
        public void setExperienceMatchScore(Double experienceMatchScore) { this.experienceMatchScore = experienceMatchScore; }

        public Double getEducationMatchScore() { return educationMatchScore; }
        public void setEducationMatchScore(Double educationMatchScore) { this.educationMatchScore = educationMatchScore; }

        public Double getContentRelevanceScore() { return contentRelevanceScore; }
        public void setContentRelevanceScore(Double contentRelevanceScore) { this.contentRelevanceScore = contentRelevanceScore; }

        public String getMatchLevel() { return matchLevel; }
        public void setMatchLevel(String matchLevel) { this.matchLevel = matchLevel; }

        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

        public Double getFinalScore() { return finalScore; }
        public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    }
}