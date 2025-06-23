package com.example.cvfilter.service;

import com.example.cvfilter.dao.CvInfoDao;
import com.example.cvfilter.dao.entity.CvInfo;
import com.example.cvfilter.dao.entity.CvStatus;
import com.example.cvfilter.service.impl.CvExtractionServiceInterface;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CvExtractionService implements CvExtractionServiceInterface {

    private final CvInfoDao cvInfoDao;

    public CvExtractionService(CvInfoDao cvInfoDao) {
        this.cvInfoDao = cvInfoDao;
    }

    private static final String[] COMMON_SKILLS = {
            "java", "python", "javascript", "html", "css", "sql", "spring", "react",
            "angular", "node", "docker", "kubernetes", "git", "jenkins", "maven",
            "gradle", "mongodb", "mysql", "postgresql", "oracle", "linux", "windows",
            "aws", "azure", "gcp", "microservices", "rest", "api", "json", "xml",
            "junit", "selenium", "agile", "scrum", "kanban"
    };

    @Override
    public CvInfo extractAndSaveCvInfo(File cvFile, Long userId, Long companyId, Long jobOfferId) throws IOException {
        CvInfo cvInfo = extractCvInfo(cvFile, userId, companyId, jobOfferId);

        cvInfo.setExtractedAt(LocalDateTime.now());
        cvInfo.setStatus(CvStatus.PROCESSED);

        return cvInfoDao.save(cvInfo);
    }

    public CvInfo extractCvInfo(File cvFile, Long userId, Long companyId, Long jobOfferId) throws IOException {
        CvInfo cvInfo = new CvInfo(userId, jobOfferId, companyId, cvFile.getAbsolutePath());
        String content = extractTextFromFile(cvFile);

        if (content != null && !content.trim().isEmpty()) {
            String cleanedContent = cleanAndNormalizeText(content);
            cvInfo.setDescription(cleanedContent.length() > 500 ? cleanedContent.substring(0, 500) + "..." : cleanedContent);
            cvInfo.setName(extractName(cleanedContent));
            cvInfo.setEmail(extractEmail(cleanedContent));
            cvInfo.setPhone(extractPhone(cleanedContent));
            cvInfo.setSkills(extractSkills(cleanedContent));
            cvInfo.setExperience(extractExperience(cleanedContent));
            cvInfo.setEducation(extractEducation(cleanedContent));
            cvInfo.setYearsOfExperience(extractYearsOfExperience(cleanedContent));
        }

        return cvInfo;
    }

    public CvInfo extractCvInfo(File cvFile, Long userId) throws IOException {
        return extractCvInfo(cvFile, userId, null, null);
    }

    private String extractTextFromFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) {
            return extractFromPdf(file);
        } else if (fileName.endsWith(".docx")) {
            return extractFromDocx(file);
        } else if (fileName.endsWith(".txt")) {
            return extractFromTxt(file);
        }
        return null;
    }

    private String extractFromPdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            if (document.isEncrypted()) return null;
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition(true);
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(Math.min(3, document.getNumberOfPages()));
            return pdfStripper.getText(document);
        }
    }

    private String extractFromDocx(File file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new FileInputStream(file))) {
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.trim().isEmpty()) {
                    content.append(text).append("\n");
                }
            }
            return content.toString();
        }
    }

    private String extractFromTxt(File file) throws IOException {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            if (content.trim().isEmpty()) {
                content = Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
            }
            return content;
        } catch (Exception e) {
            return null;
        }
    }

    private String cleanAndNormalizeText(String text) {
        if (text == null) return "";
        return text.replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                .replaceAll("\\s+", " ")
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "")
                .trim();
    }

    private String extractName(String content) {
        if (content == null || content.trim().isEmpty()) return null;
        String[] lines = content.split("\n");

        for (int i = 0; i < Math.min(10, lines.length); i++) {
            String line = lines[i].trim();
            if (line.length() > 2 && line.length() < 60) {
                if (line.matches("^[A-Za-zÀ-ÿ\\s'.-]+$")) {
                    String[] words = line.split("\\s+");
                    if (words.length >= 2 && words.length <= 4) {
                        String lowerLine = line.toLowerCase();
                        if (!lowerLine.contains("cv") && !lowerLine.contains("curriculum") &&
                                !lowerLine.contains("resume") && !lowerLine.contains("téléphone") &&
                                !lowerLine.contains("email") && !lowerLine.contains("adresse")) {
                            return capitalizeWords(line);
                        }
                    }
                }
            }
        }

        Pattern[] namePatterns = {
                Pattern.compile("(?:nom|name|prénom|prenom)\\s*:?\\s*([A-Za-zÀ-ÿ\\s'.-]+)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("^([A-Za-zÀ-ÿ]+\\s+[A-Za-zÀ-ÿ]+)", Pattern.MULTILINE)
        };

        for (Pattern pattern : namePatterns) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String name = matcher.group(1).trim();
                if (name.length() > 2 && name.length() < 60) {
                    return capitalizeWords(name);
                }
            }
        }

        return null;
    }

    private String extractEmail(String content) {
        if (content == null) return null;
        Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9]([A-Za-z0-9._%-]*[A-Za-z0-9])?@[A-Za-z0-9]([A-Za-z0-9.-]*[A-Za-z0-9])?\\.[A-Za-z]{2,}\\b");
        Matcher matcher = emailPattern.matcher(content);
        return matcher.find() ? matcher.group().toLowerCase() : null;
    }

    private String extractPhone(String content) {
        if (content == null) return null;
        Pattern[] phonePatterns = {
                Pattern.compile("(?:\\+33|0)[1-9](?:[\\s.-]?\\d{2}){4}"),
                Pattern.compile("\\+?\\d{1,4}[\\s.-]?\\(?\\d{1,4}\\)?[\\s.-]?\\d{1,4}[\\s.-]?\\d{1,4}[\\s.-]?\\d{1,4}"),
                Pattern.compile("\\b\\d{10}\\b"),
                Pattern.compile("\\b\\d{2}[\\s.-]\\d{2}[\\s.-]\\d{2}[\\s.-]\\d{2}[\\s.-]\\d{2}\\b")
        };

        for (Pattern pattern : phonePatterns) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String phone = matcher.group().trim();
                String digitsOnly = phone.replaceAll("\\D", "");
                if (digitsOnly.length() >= 8 && digitsOnly.length() <= 15) {
                    return phone;
                }
            }
        }
        return null;
    }

    private String extractSkills(String content) {
        if (content == null) return null;
        String lowerContent = content.toLowerCase();
        StringBuilder skills = new StringBuilder();

        String[] skillsKeywords = {
                "compétences", "compétence", "skills", "savoir-faire", "aptitudes",
                "technologies", "technical skills", "outils", "logiciels", "connaissances"
        };

        for (String keyword : skillsKeywords) {
            int index = lowerContent.indexOf(keyword);
            if (index != -1) {
                int startIndex = index;
                int endIndex = findSectionEnd(lowerContent, index + keyword.length(),
                        new String[]{"expérience", "experience", "formation", "education", "diplôme", "langues", "centres d'intérêt", "loisirs"});
                if (endIndex > startIndex) {
                    String skillsSection = content.substring(startIndex, endIndex);
                    skills.append(cleanText(skillsSection)).append(" ");
                }
            }
        }

        List<String> foundSkills = new ArrayList<>();
        for (String skill : COMMON_SKILLS) {
            if (lowerContent.contains(skill.toLowerCase())) {
                foundSkills.add(skill);
            }
        }

        if (!foundSkills.isEmpty()) {
            skills.append("Compétences identifiées: ").append(String.join(", ", foundSkills));
        }

        String result = skills.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private String extractExperience(String content) {
        if (content == null) return null;
        String lowerContent = content.toLowerCase();
        String[] expKeywords = {
                "expérience", "expériences professionnelles", "experience", "work experience",
                "professional experience", "emploi", "carrière", "parcours professionnel", "historique"
        };

        for (String keyword : expKeywords) {
            int index = lowerContent.indexOf(keyword);
            if (index != -1) {
                int startIndex = index;
                int endIndex = findSectionEnd(lowerContent, index + keyword.length(),
                        new String[]{"formation", "education", "diplôme", "compétences", "langues", "centres d'intérêt"});
                if (endIndex > startIndex) {
                    String expSection = content.substring(startIndex, endIndex);
                    return cleanText(expSection);
                }
            }
        }
        return null;
    }

    private String extractEducation(String content) {
        if (content == null) return null;
        String lowerContent = content.toLowerCase();
        String[] eduKeywords = {
                "formation", "formations", "education", "diplômes", "diplôme", "études",
                "academic", "university", "université", "école", "degree", "bts", "master",
                "licence", "baccalauréat", "doctorat", "ingénieur"
        };

        for (String keyword : eduKeywords) {
            int index = lowerContent.indexOf(keyword);
            if (index != -1) {
                int startIndex = index;
                int endIndex = findSectionEnd(lowerContent, index + keyword.length(),
                        new String[]{"expérience", "experience", "compétences", "langues", "centres d'intérêt", "loisirs"});
                if (endIndex > startIndex) {
                    String eduSection = content.substring(startIndex, endIndex);
                    return cleanText(eduSection);
                }
            }
        }
        return null;
    }

    private String extractYearsOfExperience(String content) {
        if (content == null) return null;
        Pattern directPattern = Pattern.compile("(\\d+)\\s*(?:years?|ans?|yrs?|\\+?)\\s*(?:of\\s*)?(?:experience|expérience|exp|xp)", Pattern.CASE_INSENSITIVE);
        Pattern dateRangePattern = Pattern.compile("(?:20\\d{2}|\\d{2})[/\\-–—](?:20\\d{2}|\\d{2}|present|now|aujourd'hui|actuel)", Pattern.CASE_INSENSITIVE);
        Pattern phrasePattern = Pattern.compile("(?:over|more than|plus de|environ)\\s*(\\d+)\\s*years", Pattern.CASE_INSENSITIVE);

        Matcher directMatcher = directPattern.matcher(content);
        if (directMatcher.find()) return directMatcher.group(1) + " years";

        Matcher phraseMatcher = phrasePattern.matcher(content);
        if (phraseMatcher.find()) return phraseMatcher.group(1) + "+ years";

        Matcher dateMatcher = dateRangePattern.matcher(content);
        List<String> dateRanges = new ArrayList<>();
        while (dateMatcher.find()) dateRanges.add(dateMatcher.group());

        if (!dateRanges.isEmpty()) {
            try {
                int totalYears = calculateTotalYearsFromRanges(dateRanges);
                return totalYears + " years";
            } catch (Exception e) {
                System.err.println("Error calculating years from date ranges: " + e.getMessage());
            }
        }

        return null;
    }

    private int calculateTotalYearsFromRanges(List<String> dateRanges) {
        int totalMonths = 0;
        for (String range : dateRanges) {
            String[] parts = range.split("[/\\-–—]");
            if (parts.length == 2) {
                try {
                    int startYear = parseYear(parts[0].trim());
                    int endYear = parseYear(parts[1].trim());
                    totalMonths += (endYear - startYear) * 12;
                } catch (Exception e) {
                    System.err.println("Error parsing date range: " + range);
                }
            }
        }
        return (int) Math.ceil(totalMonths / 12.0);
    }

    private int parseYear(String yearStr) {
        if (yearStr.equalsIgnoreCase("present") ||
                yearStr.equalsIgnoreCase("now") ||
                yearStr.equalsIgnoreCase("aujourd'hui") ||
                yearStr.equalsIgnoreCase("actuel")) {
            return java.time.Year.now().getValue();
        }
        if (yearStr.length() == 2) {
            int year = Integer.parseInt(yearStr);
            return year > 50 ? 1900 + year : 2000 + year;
        }
        return Integer.parseInt(yearStr);
    }

    private int findSectionEnd(String lowerContent, int startPos, String[] endKeywords) {
        int endIndex = lowerContent.length();
        for (String endKeyword : endKeywords) {
            int endPos = lowerContent.indexOf(endKeyword, startPos);
            if (endPos != -1 && endPos < endIndex) endIndex = endPos;
        }
        return Math.min(endIndex, startPos + 800);
    }

    private String cleanText(String text) {
        if (text == null) return null;
        return text.replaceAll("\\s+", " ").replaceAll("\\n+", " ").trim();
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }









}