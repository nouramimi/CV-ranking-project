package com.example.cvfilter.service;

import com.example.cvfilter.dao.entity.CvInfo;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CvExtractionService implements CvExtractionServiceInterface {

    private static final String[] COMMON_SKILLS = {
            "java", "python", "javascript", "html", "css", "sql", "spring", "react",
            "angular", "node", "docker", "kubernetes", "git", "jenkins", "maven",
            "gradle", "mongodb", "mysql", "postgresql", "oracle", "linux", "windows",
            "aws", "azure", "gcp", "microservices", "rest", "api", "json", "xml",
            "junit", "selenium", "agile", "scrum", "kanban"
    };



    public CvInfo extractCvInfo(File cvFile, Long userId, Long companyId, Long jobOfferId) throws IOException {
        CvInfo cvInfo = new CvInfo(userId, jobOfferId, companyId, cvFile.getAbsolutePath());

        System.out.println("Processing CV file: " + cvFile.getName() + " for Job ID: " + jobOfferId);

        String content = extractTextFromFile(cvFile);

        if (content != null && !content.trim().isEmpty()) {
            System.out.println("Extracted content length: " + content.length());
            System.out.println("First 500 characters: " + content.substring(0, Math.min(500, content.length())));

            String cleanedContent = cleanAndNormalizeText(content);

            // Set description (first 500 characters of cleaned content)
            cvInfo.setDescription(cleanedContent.length() > 500 ?
                    cleanedContent.substring(0, 500) + "..." : cleanedContent);

            // Extract information avec contenu nettoyé
            cvInfo.setName(extractName(cleanedContent));
            cvInfo.setEmail(extractEmail(cleanedContent));
            cvInfo.setPhone(extractPhone(cleanedContent));
            cvInfo.setSkills(extractSkills(cleanedContent));
            cvInfo.setExperience(extractExperience(cleanedContent));
            cvInfo.setEducation(extractEducation(cleanedContent));

            System.out.println("Extracted - Name: " + cvInfo.getName());
            System.out.println("Extracted - Email: " + cvInfo.getEmail());
            System.out.println("Extracted - Phone: " + cvInfo.getPhone());
            System.out.println("Extracted - Skills: " +
                    (cvInfo.getSkills() != null ? cvInfo.getSkills().substring(0, Math.min(100, cvInfo.getSkills().length())) + "..." : "null"));
            System.out.println("Extracted - Experience: " +
                    (cvInfo.getExperience() != null ? cvInfo.getExperience().substring(0, Math.min(100, cvInfo.getExperience().length())) + "..." : "null"));
            System.out.println("Extracted - Education: " +
                    (cvInfo.getEducation() != null ? cvInfo.getEducation().substring(0, Math.min(100, cvInfo.getEducation().length())) + "..." : "null"));
        } else {
            System.out.println("No content extracted from file: " + cvFile.getName());
            System.out.println("File exists: " + cvFile.exists());
            System.out.println("File size: " + cvFile.length() + " bytes");
        }

        return cvInfo;
    }
    public CvInfo extractCvInfo(File cvFile, Long userId) throws IOException {

        return extractCvInfo(cvFile, userId, null, null);
    }


    private String extractTextFromFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();

        try {
            if (fileName.endsWith(".pdf")) {
                return extractFromPdf(file);
            } else if (fileName.endsWith(".docx")) {
                return extractFromDocx(file);
            } else if (fileName.endsWith(".txt")) {
                return extractFromTxt(file);
            }
        } catch (Exception e) {
            System.err.println("Error extracting text from file: " + file.getName() + " - " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private String extractFromPdf(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            if (document.isEncrypted()) {
                System.err.println("PDF is encrypted: " + file.getName());
                return null;
            }

            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition(true);
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(Math.min(3, document.getNumberOfPages())); // Limiter aux 3 premières pages

            String text = pdfStripper.getText(document);
            System.out.println("PDF extraction successful, text length: " + text.length());

            if (text.trim().isEmpty()) {
                System.err.println("PDF text extraction returned empty content for: " + file.getName());
            }

            return text;
        } catch (Exception e) {
            System.err.println("Error extracting PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
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
            String result = content.toString();
            System.out.println("DOCX extraction successful, text length: " + result.length());
            return result;
        } catch (Exception e) {
            System.err.println("Error extracting DOCX: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String extractFromTxt(File file) throws IOException {
        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            if (content.trim().isEmpty()) {
                content = Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
            }
            System.out.println("TXT extraction successful, text length: " + content.length());
            return content;
        } catch (Exception e) {
            System.err.println("Error extracting TXT: " + e.getMessage());
            return null;
        }
    }

    private String cleanAndNormalizeText(String text) {
        if (text == null) return "";

        return text
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                .replaceAll("\\s+", " ")
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "") // Supprimer les caractères de contrôle
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

        Pattern emailPattern = Pattern.compile(
                "\\b[A-Za-z0-9]([A-Za-z0-9._%-]*[A-Za-z0-9])?@[A-Za-z0-9]([A-Za-z0-9.-]*[A-Za-z0-9])?\\.[A-Za-z]{2,}\\b"
        );

        Matcher matcher = emailPattern.matcher(content);
        if (matcher.find()) {
            return matcher.group().toLowerCase();
        }
        return null;
    }

    private String extractPhone(String content) {
        if (content == null) return null;

        Pattern[] phonePatterns = {
                Pattern.compile("(?:\\+33|0)[1-9](?:[\\s.-]?\\d{2}){4}"), // Format français standard
                Pattern.compile("\\+?\\d{1,4}[\\s.-]?\\(?\\d{1,4}\\)?[\\s.-]?\\d{1,4}[\\s.-]?\\d{1,4}[\\s.-]?\\d{1,4}"), // Format international
                Pattern.compile("\\b\\d{10}\\b"), // 10 chiffres consécutifs
                Pattern.compile("\\b\\d{2}[\\s.-]\\d{2}[\\s.-]\\d{2}[\\s.-]\\d{2}[\\s.-]\\d{2}\\b") // Format avec séparateurs
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

    private int findSectionEnd(String lowerContent, int startPos, String[] endKeywords) {
        int endIndex = lowerContent.length();

        for (String endKeyword : endKeywords) {
            int endPos = lowerContent.indexOf(endKeyword, startPos);
            if (endPos != -1 && endPos < endIndex) {
                endIndex = endPos;
            }
        }

        // Limiter à 800 caractères maximum par section
        return Math.min(endIndex, startPos + 800);
    }

    private String cleanText(String text) {
        if (text == null) return null;
        return text.replaceAll("\\s+", " ")
                .replaceAll("\\n+", " ")
                .trim();
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