package com.example.cvfilter.service;

import com.example.cvfilter.dao.entity.CvInfo;
import com.example.cvfilter.service.impl.CvExtractionServiceInterface;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CvProcessingService {

    @Value("${cv.storage.path:data}")
    private String storagePath;

    @Value("${cv.extracted.info.file:cv_extracted_info.csv}")
    private String extractedInfoFile;

    private final CvExtractionServiceInterface cvExtractionService;

    private Set<String> processedFiles = new HashSet<>();
    private Set<String> existingCvRecords = new HashSet<>();

    public CvProcessingService(CvExtractionService cvExtractionService) {
        this.cvExtractionService = cvExtractionService;
    }

    @PostConstruct
    public void init() {
        loadExistingCvRecords();
    }

    private void loadExistingCvRecords() {
        if (extractedInfoFile == null) {
            System.out.println("extractedInfoFile is null, skipping loading existing records");
            return;
        }

        File csvFile = new File(extractedInfoFile);
        if (!csvFile.exists()) {
            System.out.println("CSV file does not exist: " + extractedInfoFile);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            // Skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                // Extract the cv_path from the CSV line
                String[] parts = parseCsvLine(line);
                if (parts.length >= 3) {
                    String cvPath = parts[2].replace("\"", "");
                    existingCvRecords.add(cvPath);
                }
            }
            System.out.println("Loaded " + existingCvRecords.size() + " existing CV records from CSV");
        } catch (IOException e) {
            System.err.println("Error loading existing CV records: " + e.getMessage());
        }
    }
    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder value = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(value.toString());
                value = new StringBuilder();
            } else {
                value.append(c);
            }
        }
        values.add(value.toString());
        return values.toArray(new String[0]);
    }

    private boolean isCvAlreadyProcessed(String cvPath) {
        return existingCvRecords.contains(cvPath);
    }

    @Scheduled(fixedRate = 60000)
    public void processCvsAndExtractInfo() {
        try {
            List<CvInfo> extractedInfos = new ArrayList<>();

            File storageDir = new File(storagePath);
            if (!storageDir.exists()) {
                System.out.println("Storage directory does not exist: " + storagePath);
                return;
            }

            File[] jobDirs = storageDir.listFiles(File::isDirectory);
            if (jobDirs == null) {
                System.out.println("No job directories found in: " + storagePath);
                return;
            }

            System.out.println("=== CV Processing Started at " + LocalDateTime.now() + " ===");
            System.out.println("Scanning " + jobDirs.length + " job directories...");

            for (File jobDir : jobDirs) {
                System.out.println("Processing job directory: " + jobDir.getName());

                // Extract job ID from directory name
                Long jobOfferId = extractJobIdFromDirectoryName(jobDir.getName());
                if (jobOfferId == null) {
                    System.out.println("  Could not extract job offer ID from directory: " + jobDir.getName());
                    continue;
                }

                File[] cvFiles = jobDir.listFiles(file -> {
                    if (!file.isFile()) return false;
                    String name = file.getName().toLowerCase();
                    return name.endsWith(".pdf") || name.endsWith(".docx") || name.endsWith(".txt");
                });

                if (cvFiles != null && cvFiles.length > 0) {
                    System.out.println("Found " + cvFiles.length + " CV files in job directory: " + jobDir.getName() + " (Job ID: " + jobOfferId + ")");

                    for (File cvFile : cvFiles) {
                        String fileKey = cvFile.getAbsolutePath() + "_" + cvFile.lastModified();

                        // Skip if already processed in this session
                        if (processedFiles.contains(fileKey)) {
                            System.out.println("  Skipping already processed file: " + cvFile.getName());
                            continue;
                        }

                        // Skip if already exists in CSV
                        if (isCvAlreadyProcessed(cvFile.getAbsolutePath())) {
                            System.out.println("  Skipping CV already in CSV: " + cvFile.getName());
                            processedFiles.add(fileKey);
                            continue;
                        }

                        try {
                            // Validate file before processing
                            if (!validateFile(cvFile)) {
                                continue;
                            }

                            Long userId = extractUserIdFromFilename(cvFile.getName());
                            if (userId != null) {
                                System.out.println("  Processing CV: " + cvFile.getName() + " for user: " + userId + " (Job: " + jobOfferId + ")");

                                CvInfo cvInfo = cvExtractionService.extractCvInfo(cvFile, userId, jobOfferId);

                                if (validateExtractedInfo(cvInfo)) {
                                    extractedInfos.add(cvInfo);
                                    processedFiles.add(fileKey);
                                    existingCvRecords.add(cvFile.getAbsolutePath());
                                    System.out.println("  ✓ Successfully processed CV for user: " + userId + " (Job: " + jobOfferId + ")");
                                } else {
                                    System.out.println("  ⚠ CV processed but no meaningful data extracted for user: " + userId + " (Job: " + jobOfferId + ")");
                                    extractedInfos.add(cvInfo);
                                    processedFiles.add(fileKey);
                                    existingCvRecords.add(cvFile.getAbsolutePath());
                                }
                            } else {
                                System.out.println("  ✗ Could not extract user ID from filename: " + cvFile.getName());
                            }
                        } catch (Exception e) {
                            System.err.println("  ✗ Error processing CV file: " + cvFile.getName() + " - " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("No valid CV files found in directory: " + jobDir.getName());
                }
            }

            if (!extractedInfos.isEmpty()) {
                saveCvInfoToCsv(extractedInfos);
                System.out.println("=== Successfully processed " + extractedInfos.size() + " CVs at " + LocalDateTime.now() + " ===");
                printExtractionStats(extractedInfos);
            } else {
                System.out.println("=== No new CVs to process at " + LocalDateTime.now() + " ===");
            }

        } catch (Exception e) {
            System.err.println("Error in CV processing scheduled task: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private Long extractJobIdFromDirectoryName(String directoryName) {
        // Pattern pour extraire l'ID du job depuis le nom du répertoire
        // Supposons que le répertoire soit nommé "job_123" ou "123" ou "job-123"
        Pattern[] patterns = {
                Pattern.compile("job[_-]?(\\d+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("^(\\d+)$"), // Juste un nombre
                Pattern.compile("(\\d+)") // N'importe quel nombre dans le nom
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(directoryName);
            if (matcher.find()) {
                try {
                    return Long.parseLong(matcher.group(1));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        System.err.println("Could not extract job offer ID from directory name: " + directoryName);
        return null;
    }

    private boolean validateFile(File file) {
        if (!file.exists()) {
            System.err.println("File does not exist: " + file.getAbsolutePath());
            return false;
        }

        if (!file.canRead()) {
            System.err.println("Cannot read file: " + file.getAbsolutePath());
            return false;
        }

        if (file.length() == 0) {
            System.err.println("File is empty: " + file.getAbsolutePath());
            return false;
        }

        if (file.length() > 50 * 1024 * 1024) { // 50MB max
            System.err.println("File too large: " + file.getAbsolutePath() + " (" + file.length() + " bytes)");
            return false;
        }

        return true;
    }

    private boolean validateExtractedInfo(CvInfo cvInfo) {
        if (cvInfo == null) return false;

        // Compter les champs non vides
        int filledFields = 0;
        if (isNotEmpty(cvInfo.getName())) filledFields++;
        if (isNotEmpty(cvInfo.getEmail())) filledFields++;
        if (isNotEmpty(cvInfo.getPhone())) filledFields++;
        if (isNotEmpty(cvInfo.getSkills())) filledFields++;
        if (isNotEmpty(cvInfo.getExperience())) filledFields++;
        if (isNotEmpty(cvInfo.getEducation())) filledFields++;

        // Au moins un champ doit être rempli pour considérer l'extraction comme réussie
        return filledFields > 0 || isNotEmpty(cvInfo.getDescription());
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void printExtractionStats(List<CvInfo> cvInfos) {
        int nameCount = 0, emailCount = 0, phoneCount = 0, skillsCount = 0, expCount = 0, eduCount = 0;

        for (CvInfo cvInfo : cvInfos) {
            if (isNotEmpty(cvInfo.getName())) nameCount++;
            if (isNotEmpty(cvInfo.getEmail())) emailCount++;
            if (isNotEmpty(cvInfo.getPhone())) phoneCount++;
            if (isNotEmpty(cvInfo.getSkills())) skillsCount++;
            if (isNotEmpty(cvInfo.getExperience())) expCount++;
            if (isNotEmpty(cvInfo.getEducation())) eduCount++;
        }

        System.out.println("=== Extraction Statistics ===");
        System.out.println("Names extracted: " + nameCount + "/" + cvInfos.size());
        System.out.println("Emails extracted: " + emailCount + "/" + cvInfos.size());
        System.out.println("Phones extracted: " + phoneCount + "/" + cvInfos.size());
        System.out.println("Skills extracted: " + skillsCount + "/" + cvInfos.size());
        System.out.println("Experience extracted: " + expCount + "/" + cvInfos.size());
        System.out.println("Education extracted: " + eduCount + "/" + cvInfos.size());
    }

    private Long extractUserIdFromFilename(String filename) {
        // Pattern plus flexible pour extraire l'user ID
        Pattern[] patterns = {
                Pattern.compile("_user_(\\d+)_"),
                Pattern.compile("user(\\d+)"),
                Pattern.compile("_(\\d+)_"),
                Pattern.compile("(\\d+)")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(filename);
            if (matcher.find()) {
                try {
                    return Long.parseLong(matcher.group(1));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        System.err.println("Could not extract user ID from filename: " + filename);
        return null;
    }

    private void saveCvInfoToCsv(List<CvInfo> cvInfos) throws IOException {
        Path csvPath = Paths.get(extractedInfoFile);
        boolean fileExists = Files.exists(csvPath);

        try (FileWriter writer = new FileWriter(extractedInfoFile, true)) {
            // Écrire l'en-tête si le fichier n'existe pas
            if (!fileExists) {
                writer.append("user_id,job_offer_id,cv_path,name,email,phone,description,skills,experience,education,extracted_at\n");
            }

            for (CvInfo cvInfo : cvInfos) {
                String timestamp = cvInfo.getExtractedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                // Format CSV avec échappement approprié
                String csvLine = String.format("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s\n",
                        cvInfo.getUserId(),
                        cvInfo.getJobOfferId(), // Added job offer ID
                        escapeCsvValue(cvInfo.getCvPath()),
                        escapeCsvValue(cvInfo.getName()),
                        escapeCsvValue(cvInfo.getEmail()),
                        escapeCsvValue(cvInfo.getPhone()),
                        escapeCsvValue(truncateDescription(cvInfo.getDescription())),
                        escapeCsvValue(truncateText(cvInfo.getSkills(), 200)),
                        escapeCsvValue(truncateText(cvInfo.getExperience(), 300)),
                        escapeCsvValue(truncateText(cvInfo.getEducation(), 200)),
                        timestamp
                );

                writer.append(csvLine);
            }

            writer.flush();
        }

        System.out.println("CSV file updated: " + extractedInfoFile);
    }

    private String escapeCsvValue(String value) {
        if (value == null) return "";

        // Échapper les guillemets en les doublant et remplacer les retours à la ligne
        return value.replace("\"", "\"\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String truncateDescription(String description) {
        if (description == null) return "";
        return description.length() > 500 ? description.substring(0, 500) + "..." : description;
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}