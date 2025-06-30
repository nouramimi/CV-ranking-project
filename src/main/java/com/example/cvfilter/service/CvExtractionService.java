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
            "java", "python", "javascript", "typescript", "html", "css", "sql",
            "spring", "react", "angular", "vue", "node", "express", "docker",
            "kubernetes", "git", "jenkins", "maven", "gradle", "mongodb", "mysql",
            "postgresql", "oracle", "linux", "windows", "aws", "azure", "gcp",
            "microservices", "rest", "api", "json", "xml", "junit", "selenium",
            "agile", "scrum", "kanban", "flutter", "php", "symfony", "laravel",
            "nest", "next", "bootstrap", "tailwind", "redis", "elasticsearch",
            "figma", "postman", "cpanel", "whm"
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
            cvInfo.setHighestDegree(extractHighestDegree(cleanedContent));
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
                "technical skills", "skills", "compétences techniques", "compétences",
                "technologies", "savoir-faire", "aptitudes", "outils", "logiciels"
        };

        Set<String> extractedSections = new LinkedHashSet<>();

        for (String keyword : skillsKeywords) {
            int index = lowerContent.indexOf(keyword);
            if (index != -1) {
                int startIndex = index;
                int endIndex = findSkillsSectionEnd(lowerContent, index + keyword.length());

                if (endIndex > startIndex) {
                    String skillsSection = content.substring(startIndex, endIndex);
                    String cleanedSection = cleanSkillsSection(skillsSection);
                    if (cleanedSection.length() > 10 && cleanedSection.length() < 500) {
                        extractedSections.add(cleanedSection);
                    }
                }
            }
        }

        for (String section : extractedSections) {
            if (skills.length() > 0) skills.append(" ");
            skills.append(section);
        }

        Map<String, List<String>> categorizedSkills = extractCategorizedSkillsOnly(content);

        for (Map.Entry<String, List<String>> entry : categorizedSkills.entrySet()) {
            String category = entry.getKey();
            List<String> categorySkills = entry.getValue();

            if (!categorySkills.isEmpty()) {
                if (skills.length() > 0) skills.append(" ");
                skills.append(category).append(": ")
                        .append(String.join(", ", categorySkills));
            }
        }

        List<String> foundSkills = extractCommonSkillsOnly(content);
        if (!foundSkills.isEmpty()) {
            if (skills.length() > 0) skills.append(" ");
            skills.append("Compétences techniques identifiées: ").append(String.join(", ", foundSkills));
        }

        String result = skills.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private int findSkillsSectionEnd(String lowerContent, int startPos) {
        String[] endKeywords = {
                "experience", "expérience", "work experience", "employment", "emploi",
                "education", "formation", "academic", "university", "école",
                "projects", "projets", "mini-projects", "project",
                "languages", "langues", "language skills",
                "certifications", "certification", "awards", "achievements",
                "interests", "centres d'intérêt", "hobbies", "loisirs",
                "references", "références", "contact", "personal"
        };

        int endIndex = lowerContent.length();

        for (String endKeyword : endKeywords) {
            int endPos = lowerContent.indexOf(endKeyword, startPos);
            if (endPos != -1 && endPos < endIndex) {
                endIndex = endPos;
            }
        }

        return Math.min(endIndex, startPos + 300);
    }

    private String cleanSkillsSection(String section) {
        if (section == null) return "";

        String cleaned = section
                .replaceAll("\\*", "")
                .replaceAll("•", "")
                .replaceAll("\\n+", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String[] lines = cleaned.split("\\.");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            String lowerLine = line.toLowerCase().trim();

            if (!lowerLine.contains("developed") &&
                    !lowerLine.contains("managed") &&
                    !lowerLine.contains("created") &&
                    !lowerLine.contains("implemented") &&
                    !lowerLine.contains("years of experience") &&
                    line.trim().length() > 3 &&
                    line.trim().length() < 100) {

                if (result.length() > 0) result.append(" ");
                result.append(line.trim());
            }
        }

        return result.toString();
    }

    private Map<String, List<String>> extractCategorizedSkillsOnly(String content) {
        Map<String, List<String>> categorizedSkills = new LinkedHashMap<>();

        String skillsSection = extractTechnicalSkillsSection(content);
        if (skillsSection == null || skillsSection.isEmpty()) {
            skillsSection = content;
        }
        String lowerSkillsSection = skillsSection.toLowerCase();

        Map<String, String[]> skillCategories = new HashMap<>();

        skillCategories.put("Frontend", new String[]{
                "react.js", "react", "angular", "vue.js", "vue", "next.js", "next",
                "html", "css", "javascript", "typescript", "bootstrap", "tailwind"
        });

        skillCategories.put("Backend", new String[]{
                "node.js", "express", "nest.js", "spring", "spring boot", "django",
                "flask", "php", "symfony", "asp.net", ".net", "laravel"
        });

        skillCategories.put("Mobile", new String[]{
                "flutter", "react native", "swift", "kotlin", "android", "ios", "xamarin"
        });

        skillCategories.put("Databases", new String[]{
                "mysql", "postgresql", "mongodb", "redis", "sqlite", "oracle",
                "sql server", "elasticsearch"
        });

        skillCategories.put("Tools", new String[]{
                "git", "docker", "kubernetes", "jenkins", "maven", "gradle",
                "figma", "cpanel", "whm", "postman"
        });

        skillCategories.put("Languages", new String[]{
                "java", "python", "javascript", "typescript", "php", "c#", "c++",
                "go", "rust", "kotlin", "swift", "sql"
        });

        for (Map.Entry<String, String[]> categoryEntry : skillCategories.entrySet()) {
            String category = categoryEntry.getKey();
            String[] skills = categoryEntry.getValue();
            List<String> foundSkills = new ArrayList<>();

            for (String skill : skills) {
                if (lowerSkillsSection.contains(skill.toLowerCase())) {
                    String capitalizedSkill = capitalizeSkill(skill);
                    if (!foundSkills.contains(capitalizedSkill)) {
                        foundSkills.add(capitalizedSkill);
                    }
                }
            }

            if (!foundSkills.isEmpty()) {
                categorizedSkills.put(category, foundSkills);
            }
        }

        return categorizedSkills;
    }

    private String extractTechnicalSkillsSection(String content) {
        if (content == null) return null;

        String lowerContent = content.toLowerCase();
        String[] patterns = {"technical skills", "skills", "compétences techniques"};

        for (String pattern : patterns) {
            int index = lowerContent.indexOf(pattern);
            if (index != -1) {
                int startIndex = index;
                int endIndex = findSkillsSectionEnd(lowerContent, index + pattern.length());

                if (endIndex > startIndex) {
                    return content.substring(startIndex, endIndex);
                }
            }
        }

        return null;
    }

    private List<String> extractCommonSkillsOnly(String content) {
        String skillsSection = extractTechnicalSkillsSection(content);
        if (skillsSection == null) {
            return new ArrayList<>();
        }

        String lowerSkillsSection = skillsSection.toLowerCase();
        List<String> foundSkills = new ArrayList<>();

        String[] importantSkills = {
                "java", "python", "javascript", "typescript", "react", "angular", "vue",
                "node", "spring", "django", "php", "sql", "mysql", "postgresql",
                "mongodb", "git", "docker", "aws", "azure", "flutter"
        };

        for (String skill : importantSkills) {
            if (lowerSkillsSection.contains(skill.toLowerCase()) && !foundSkills.contains(skill)) {
                foundSkills.add(skill);
            }
        }

        return foundSkills;
    }

    private String capitalizeSkill(String skill) {
        if (skill == null) return skill;

        Map<String, String> specialCases = new HashMap<>();
        specialCases.put("javascript", "JavaScript");
        specialCases.put("typescript", "TypeScript");
        specialCases.put("react.js", "React.js");
        specialCases.put("node.js", "Node.js");
        specialCases.put("next.js", "Next.js");
        specialCases.put("vue.js", "Vue.js");
        specialCases.put("nest.js", "Nest.js");
        specialCases.put("asp.net", "ASP.NET");
        specialCases.put(".net", ".NET");
        specialCases.put("mysql", "MySQL");
        specialCases.put("postgresql", "PostgreSQL");
        specialCases.put("mongodb", "MongoDB");
        specialCases.put("cpanel", "cPanel");
        specialCases.put("whm", "WHM");

        String lowerSkill = skill.toLowerCase();
        if (specialCases.containsKey(lowerSkill)) {
            return specialCases.get(lowerSkill);
        }

        return skill.substring(0, 1).toUpperCase() + skill.substring(1).toLowerCase();
    }

    private String extractExperience(String content) {
        if (content == null) return null;
        String lowerContent = content.toLowerCase();
        StringBuilder experience = new StringBuilder();

        String[] expKeywords = {
                "expérience", "expériences professionnelles", "experience", "work experience",
                "professional experience", "emploi", "carrière", "parcours professionnel", "historique",
                "internships", "internship", "stages", "stage", "stagiaire"
        };

        for (String keyword : expKeywords) {
            int index = lowerContent.indexOf(keyword);
            if (index != -1) {
                int startIndex = index;
                int endIndex = findSectionEnd(lowerContent, index + keyword.length(),
                        new String[]{"formation", "education", "diplôme", "compétences", "skills",
                                "langues", "languages", "centres d'intérêt", "projects", "projets"});
                if (endIndex > startIndex) {
                    String expSection = content.substring(startIndex, endIndex);
                    experience.append(cleanText(expSection)).append(" ");
                }
            }
        }

        List<String> detectedPositions = extractSpecificPositions(content);
        if (!detectedPositions.isEmpty()) {
            experience.append("Postes identifiés: ").append(String.join(", ", detectedPositions)).append(" ");
        }

        String result = experience.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private List<String> extractSpecificPositions(String content) {
        Set<String> positions = new LinkedHashSet<>();
        String[] lines = content.split("\n");

        String[] positionKeywords = {
                "developer", "développeur", "intern", "stagiaire", "engineer", "ingénieur",
                "analyst", "analyste", "manager", "consultant", "designer", "architect",
                "full stack", "frontend", "backend", "web development", "mobile development"
        };

        for (String line : lines) {
            String lowerLine = line.toLowerCase().trim();

            for (String keyword : positionKeywords) {
                if (lowerLine.contains(keyword) &&
                        line.trim().length() > 5 &&
                        line.trim().length() < 100 &&
                        !lowerLine.contains("technologies:") &&
                        !lowerLine.contains("skills:")) {

                    String cleanPosition = line.trim()
                            .replaceAll("\\*", "")
                            .replaceAll("•", "")
                            .trim();

                    if (cleanPosition.length() > 3) {
                        positions.add(cleanPosition);
                    }
                    break;
                }
            }
        }

        return new ArrayList<>(positions);
    }

    private String extractEducation(String content) {
        if (content == null) return null;

        String lowerContent = content.toLowerCase();
        Set<String> educationInfo = new LinkedHashSet<>();

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
                    String cleaned = cleanText(eduSection);
                    if (cleaned.length() > 20) {
                        educationInfo.add(cleaned);
                    }
                }
            }
        }

        List<String> institutions = extractInstitutions(content);
        List<String> degrees = extractSpecificDegrees(content);

        StringBuilder result = new StringBuilder();

        for (String section : educationInfo) {
            if (result.length() > 0) result.append(" ");
            result.append(section);
        }

        if (!institutions.isEmpty()) {
            if (result.length() > 0) result.append(" ");
            result.append("Institutions: ").append(String.join(", ", institutions));
        }

        if (!degrees.isEmpty()) {
            if (result.length() > 0) result.append(" ");
            result.append("Diplômes: ").append(String.join(", ", degrees));
        }

        String finalResult = result.toString().trim();
        finalResult = removeDuplicateContent(finalResult);

        return finalResult.isEmpty() ? null : finalResult;
    }

    private String removeDuplicateContent(String text) {
        if (text == null || text.length() < 50) return text;

        String[] sentences = text.split("\\. |\\s{2,}");
        Set<String> uniqueSentences = new LinkedHashSet<>();

        for (String sentence : sentences) {
            String normalized = sentence.trim().toLowerCase();
            if (normalized.length() > 10) {
                boolean isDuplicate = false;
                for (String existing : uniqueSentences) {
                    if (existing.toLowerCase().contains(normalized) ||
                            normalized.contains(existing.toLowerCase())) {
                        isDuplicate = true;
                        break;
                    }
                }
                if (!isDuplicate) {
                    uniqueSentences.add(sentence.trim());
                }
            }
        }

        return String.join(" ", uniqueSentences);
    }

    private List<String> extractSpecificDegrees(String content) {
        Set<String> degrees = new LinkedHashSet<>();
        String lowerContent = content.toLowerCase();

        Map<String, String[]> degreePatterns = new HashMap<>();
        degreePatterns.put("Software Engineering", new String[]{"software engineering degree", "software engineering"});
        degreePatterns.put("Computer Science", new String[]{"computer science", "informatique"});
        degreePatterns.put("Bachelor", new String[]{"bachelor", "licence"});
        degreePatterns.put("Master", new String[]{"master", "maîtrise"});
        degreePatterns.put("PhD", new String[]{"phd", "doctorate", "doctorat"});
        degreePatterns.put("Engineering", new String[]{"engineering degree", "diplôme d'ingénieur"});
        degreePatterns.put("Baccalaureate", new String[]{"baccalaureate", "baccalauréat"});

        for (Map.Entry<String, String[]> entry : degreePatterns.entrySet()) {
            String degreeName = entry.getKey();
            String[] patterns = entry.getValue();

            for (String pattern : patterns) {
                if (lowerContent.contains(pattern)) {
                    degrees.add(degreeName);
                    break;
                }
            }
        }

        return new ArrayList<>(degrees);
    }

    private List<String> extractInstitutions(String content) {
        Set<String> institutions = new LinkedHashSet<>();

        String[] institutionKeywords = {
                "institute", "institut", "university", "université", "college", "école",
                "school", "insat", "issat", "enit", "ensi", "esprit", "tek-up", "supcom"
        };

        String[] lines = content.split("\n");
        for (String line : lines) {
            String lowerLine = line.toLowerCase().trim();

            for (String keyword : institutionKeywords) {
                if (lowerLine.contains(keyword) &&
                        line.trim().length() > 10 &&
                        line.trim().length() < 100 &&
                        !lowerLine.contains("experience") &&
                        !lowerLine.contains("compétence")) {

                    String cleanInstitution = line.trim();
                    institutions.add(cleanInstitution);
                    break;
                }
            }
        }

        return new ArrayList<>(institutions);
    }

    private String extractHighestDegree(String content) {
        if (content == null) return null;

        String lowerContent = content.toLowerCase();

        Map<String, Integer> degreeHierarchy = new HashMap<>();
        degreeHierarchy.put("PHD", 6);
        degreeHierarchy.put("DOCTORATE", 6);
        degreeHierarchy.put("DOCTORAT", 6);
        degreeHierarchy.put("MASTER", 5);
        degreeHierarchy.put("MASTER'S", 5);
        degreeHierarchy.put("MAITRISE", 5);
        degreeHierarchy.put("MBA", 5);
        degreeHierarchy.put("ENGINEERING", 4);
        degreeHierarchy.put("ENGINEER", 4);
        degreeHierarchy.put("INGENIEUR", 4);
        degreeHierarchy.put("BACHELOR", 3);
        degreeHierarchy.put("LICENCE", 3);
        degreeHierarchy.put("BTS", 2);
        degreeHierarchy.put("ASSOCIATE", 2);
        degreeHierarchy.put("BACCALAUREATE", 1);
        degreeHierarchy.put("BACCALAUREAT", 1);
        degreeHierarchy.put("BAC", 1);

        String highestDegree = null;
        int highestLevel = 0;

        Pattern degreePattern = Pattern.compile("([a-zA-Z\\s]+)\\s+degree", Pattern.CASE_INSENSITIVE);
        Matcher degreeMatcher = degreePattern.matcher(content);

        while (degreeMatcher.find()) {
            String degreeText = degreeMatcher.group(1).trim();
            String normalizedDegree = normalizeDegree(degreeText);
            Integer level = degreeHierarchy.get(normalizedDegree.toUpperCase());

            if (level != null && level > highestLevel) {
                highestLevel = level;
                highestDegree = normalizedDegree;
            }
        }

        for (Map.Entry<String, Integer> entry : degreeHierarchy.entrySet()) {
            String degree = entry.getKey();
            Integer level = entry.getValue();

            if (lowerContent.contains(degree.toLowerCase()) && level > highestLevel) {
                highestLevel = level;
                highestDegree = degree;
            }
        }

        if (lowerContent.contains("software engineering") && highestLevel < 4) {
            highestDegree = "SOFTWARE_ENGINEERING";
            highestLevel = 4;
        }

        if (lowerContent.contains("computer science") && highestLevel < 4) {
            highestDegree = "COMPUTER_SCIENCE";
            highestLevel = 4;
        }

        return highestDegree;
    }

    private String normalizeDegree(String degreeText) {
        if (degreeText == null) return null;

        String lower = degreeText.toLowerCase().trim();

        if (lower.contains("software engineering") || lower.contains("génie logiciel")) {
            return "SOFTWARE_ENGINEERING";
        }
        if (lower.contains("computer science") || lower.contains("informatique")) {
            return "COMPUTER_SCIENCE";
        }
        if (lower.contains("engineering") || lower.contains("ingénieur")) {
            return "ENGINEERING";
        }
        if (lower.contains("master") || lower.contains("maîtrise")) {
            return "MASTER";
        }
        if (lower.contains("bachelor") || lower.contains("licence")) {
            return "BACHELOR";
        }
        if (lower.contains("phd") || lower.contains("doctorate") || lower.contains("doctorat")) {
            return "PHD";
        }
        if (lower.contains("baccalaureate") || lower.contains("baccalauréat") || lower.equals("bac")) {
            return "BACCALAUREATE";
        }
        if (lower.contains("bts")) {
            return "BTS";
        }
        if (lower.contains("associate")) {
            return "ASSOCIATE";
        }

        return degreeText.toUpperCase().replace(" ", "_");
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