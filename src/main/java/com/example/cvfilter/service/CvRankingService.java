package com.example.cvfilter.service;

import com.example.cvfilter.dao.entity.CvInfo;
import com.example.cvfilter.dao.entity.CvRanking;
import com.example.cvfilter.dao.entity.JobOffer;
import com.example.cvfilter.exception.CvUploadException;
import com.example.cvfilter.exception.InvalidJobOfferException;
import com.example.cvfilter.exception.JobOfferNotFoundException;
import com.example.cvfilter.service.impl.AuthorizationServiceInterface;
import com.example.cvfilter.service.impl.CvRankingServiceInterface;
import com.example.cvfilter.service.impl.EmailServiceInterface;
import com.example.cvfilter.service.impl.JobOfferServiceInterface;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CvRankingService implements CvRankingServiceInterface {

    @Value("${cv.extracted.info.file:cv_extracted_info.csv}")
    private String extractedInfoFile;

    private final JobOfferServiceInterface jobOfferService;
    private final AuthorizationServiceInterface authorizationService;
    private final EmailServiceInterface emailService;

    private static final Set<String> STOPWORDS = Set.of(
            "le", "de", "et", "à", "un", "il", "être", "en", "avoir", "que", "pour",
            "dans", "ce", "son", "une", "sur", "avec", "ne", "se", "pas", "tout", "plus",
            "par", "grand", "the", "be", "to", "of", "and", "a", "in", "that", "have",
            "i", "it", "for", "not", "on", "with", "he", "as", "you", "do", "at"
    );

    public CvRankingService(JobOfferServiceInterface jobOfferService, AuthorizationServiceInterface authorizationService, EmailServiceInterface emailService) {
        this.jobOfferService = jobOfferService;
        this.authorizationService = authorizationService;
        this.emailService = emailService;
    }

    /*@Override
    public List<CvRanking> getBestCvsForJob(Long jobOfferId, String username) {
        List<CvRanking> rankings = getTopCvsForJob(jobOfferId, 5, username);

        // Assigner les rangs
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return rankings;
    }*/
    @Override
    public List<CvRanking> getBestCvsForJob(Long jobOfferId, String username) {
        // Debug log
        System.out.println("getBestCvsForJob called for job " + jobOfferId + " by " + username);

        JobOffer jobOffer = jobOfferService.getById(jobOfferId, username)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found: " + jobOfferId));

        // Debug company access
        System.out.println("Checking access for company " + jobOffer.getCompanyId());
        authorizationService.checkCompanyAccess(username, jobOffer.getCompanyId());

        // Load CVs with debug
        List<CvInfo> cvs;
        try {
            cvs = loadCvsForJobOffer(jobOfferId, jobOffer.getCompanyId());
            System.out.println("Loaded " + cvs.size() + " CVs after filtering");
        } catch (IOException e) {
            throw new CvUploadException("Failed to load CVs for job offer " + jobOfferId, e);
        }

        if (cvs.isEmpty()) {
            System.out.println("No CVs found after loading");
            return Collections.emptyList();
        }

        // Rank CVs
        List<CvRanking> rankings = rankCvs(jobOffer.getDescription(), cvs, Math.min(5, cvs.size()));
        System.out.println("Generated " + rankings.size() + " rankings");

        // Assign ranks
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return rankings;
    }

    @Override
    public List<CvRanking> getTopCvsForJob(Long jobOfferId, int topN, String username) {
        // Validation des paramètres
        if (topN <= 0 || topN > 20) {
            throw new IllegalArgumentException("topN must be between 1 and 20");
        }

        JobOffer jobOffer = jobOfferService.getById(jobOfferId, username)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found: " + jobOfferId));

        // Vérifier les permissions d'accès à l'entreprise
        authorizationService.checkCompanyAccess(username, jobOffer.getCompanyId());

        String jobDescription = jobOffer.getDescription();
        if (jobDescription == null || jobDescription.trim().isEmpty()) {
            throw new InvalidJobOfferException("Job offer description is empty");
        }

        List<CvInfo> cvs;
        try {
            cvs = loadCvsForJobOffer(jobOfferId, jobOffer.getCompanyId());
        } catch (IOException e) {
            throw new CvUploadException("Failed to load CVs for job offer " + jobOfferId, e);
        }

        if (cvs.isEmpty()) {
            return Collections.emptyList();
        }

        List<CvRanking> rankings = rankCvs(jobDescription, cvs, Math.min(topN, cvs.size()));

        // Assigner les rangs
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return rankings;
    }

    @Override
    public int getBestCvsAndNotify(Long jobOfferId, String username) {
        List<CvRanking> rankings = getBestCvsForJob(jobOfferId, username);

        if (rankings.isEmpty()) {
            return 0;
        }

        emailService.sendAcceptanceEmails(rankings, jobOfferId);
        return rankings.size();
    }

    @Override
    public int getTopCvsAndNotify(Long jobOfferId, int topN, String username) {
        // Validation des paramètres
        if (topN <= 0 || topN > 20) {
            throw new IllegalArgumentException("topN must be between 1 and 20");
        }

        List<CvRanking> rankings = getTopCvsForJob(jobOfferId, topN, username);

        if (rankings.isEmpty()) {
            return 0;
        }

        emailService.sendAcceptanceEmails(rankings, jobOfferId);
        return rankings.size();
    }
    @Override
    public CvRanking getRankingDetails(Long jobOfferId, String username) {
        // 1. Récupérer l'offre d'emploi et vérifier les permissions
        JobOffer jobOffer = jobOfferService.getById(jobOfferId, username)
                .orElseThrow(() -> new JobOfferNotFoundException("Job offer not found: " + jobOfferId));
        authorizationService.checkCompanyAccess(username, jobOffer.getCompanyId());

        // 2. Créer un objet DTO pour contenir toutes les informations nécessaires
        // (Note: Dans une vraie application, vous devriez créer une classe DTO spécifique)
        CvRanking rankingDetails = new CvRanking();
        rankingDetails.setRankedAt(LocalDateTime.now());

        // 3. Charger tous les CVs pour cette offre
        List<CvInfo> allCvs;
        try {
            allCvs = loadCvsForJobOffer(jobOfferId, jobOffer.getCompanyId());
        } catch (IOException e) {
            throw new CvUploadException("Failed to load CVs for job offer " + jobOfferId, e);
        }

        if (allCvs.isEmpty()) {
            return rankingDetails;
        }

        // 4. Classer tous les CVs
        List<CvRanking> allRankings = rankCvs(jobOffer.getDescription(), allCvs, allCvs.size());

        // 5. Trouver le meilleur CV (le premier après tri)
        CvRanking topRanking = allRankings.stream()
                .max(Comparator.comparingDouble(CvRanking::getSimilarityScore))
                .orElse(null);

        if (topRanking != null) {
            // 6. Copier les informations du meilleur CV dans l'objet résultat
            rankingDetails.setCvInfo(topRanking.getCvInfo());
            rankingDetails.setSimilarityScore(topRanking.getSimilarityScore());
            rankingDetails.setRank(1); // C'est le top 1
        }

        return rankingDetails;
    }

    /*@Override
    public CvRanking getRankingDetails(Long jobOfferId, String username) {
        List<CvRanking> rankings = getBestCvsForJob(jobOfferId, username);

        CvRanking details = new CvRanking();
        details.setJobOfferId(jobOfferId);
        details.setTotalCvs(rankings.size());
        details.setRankings(rankings);

        if (!rankings.isEmpty()) {
            double maxScore = rankings.stream().mapToDouble(CvRanking::getSimilarityScore).max().orElse(0.0);
            double minScore = rankings.stream().mapToDouble(CvRanking::getSimilarityScore).min().orElse(0.0);
            double avgScore = rankings.stream().mapToDouble(CvRanking::getSimilarityScore).average().orElse(0.0);

            details.setMaxSimilarityScore(maxScore);
            details.setMinSimilarityScore(minScore);
            details.setAverageSimilarityScore(avgScore);
        }

        return details;
    }*/
    /*private List<CvInfo> loadCvsForJobOffer(Long jobOfferId, Long companyId) throws IOException {
        List<CvInfo> cvs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(extractedInfoFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                CvInfo cvInfo = parseCsvLine(line);
                if (cvInfo != null &&
                        jobOfferId.equals(cvInfo.getJobOfferId()) &&
                        companyId.equals(cvInfo.getCompanyId())) { // Vérification de l'entreprise
                    cvs.add(cvInfo);
                }
            }
        }

        System.out.println("Loaded " + cvs.size() + " CVs for job offer " + jobOfferId + " and company " + companyId);
        return cvs;
    }
*/
    private List<CvInfo> loadCvsForJobOffer(Long jobOfferId, Long companyId) throws IOException {
        List<CvInfo> cvs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(extractedInfoFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                CvInfo cvInfo = parseCsvLine(line);
                if (cvInfo != null && jobOfferId.equals(cvInfo.getJobOfferId())) {
                    // Ne vérifiez plus companyId s'il n'est pas dans le CSV
                    cvs.add(cvInfo);
                }
            }
        }

        System.out.println("Successfully loaded " + cvs.size() + " CVs");
        return cvs;
    }
    /*private CvInfo parseCsvLine(String line) {
        try {
            List<String> fields = parseCsvFields(line);

            if (fields.size() < 12) { // Maintenant 12 champs avec companyId
                return null;
            }

            Long userId = Long.parseLong(fields.get(0));
            Long jobOfferId = Long.parseLong(fields.get(1));
            Long companyId = Long.parseLong(fields.get(2)); // Nouveau champ
            String cvPath = fields.get(3);

            CvInfo cvInfo = new CvInfo(userId, jobOfferId, companyId, cvPath);
            cvInfo.setName(fields.get(4));
            cvInfo.setEmail(fields.get(5));
            cvInfo.setPhone(fields.get(6));
            cvInfo.setDescription(fields.get(7));
            cvInfo.setSkills(fields.get(8));
            cvInfo.setExperience(fields.get(9));
            cvInfo.setEducation(fields.get(10));

            String timestamp = fields.get(11);
            cvInfo.setExtractedAt(LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return cvInfo;
        } catch (Exception e) {
            System.err.println("Error parsing CSV line: " + line + " - " + e.getMessage());
            return null;
        }
    }*/
    private CvInfo parseCsvLine(String line) {
        try {
            List<String> fields = parseCsvFields(line);
            System.out.println("Number of fields: " + fields.size()); // Debug

            // Changez de 12 à 11 car le companyId semble manquer
            if (fields.size() < 11) {
                System.err.println("Line has insufficient fields (" + fields.size() + ")");
                return null;
            }

            Long userId = Long.parseLong(fields.get(0));
            Long jobOfferId = Long.parseLong(fields.get(1));
            String cvPath = fields.get(2);

            // Créez CvInfo sans companyId (ou avec une valeur par défaut)
            CvInfo cvInfo = new CvInfo(userId, jobOfferId, 1L, cvPath); // 1L comme companyId par défaut

            // Remplissez les autres champs
            cvInfo.setName(fields.get(3));
            cvInfo.setEmail(fields.get(4));
            cvInfo.setPhone(fields.get(5));
            cvInfo.setDescription(fields.get(6));
            cvInfo.setSkills(fields.get(7));
            cvInfo.setExperience(fields.get(8));
            cvInfo.setEducation(fields.get(9));

            // Gestion du timestamp (peut être au champ 10 si companyId est absent)
            if (fields.size() > 10) {
                String timestamp = fields.get(10);
                cvInfo.setExtractedAt(LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            return cvInfo;
        } catch (Exception e) {
            System.err.println("Error parsing CSV line: " + e.getMessage());
            return null;
        }
    }

    private List<String> parseCsvFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"' && !inQuotes) {
                inQuotes = true;
            } else if (c == '"' && inQuotes) {
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++;
                } else {
                    inQuotes = false;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString());
        return fields;
    }


    private List<CvRanking> rankCvs(String jobDescription, List<CvInfo> cvs, int topN) {
        List<String> documents = new ArrayList<>();
        documents.add(preprocessText(jobDescription)); // Job description at index 0

        for (CvInfo cv : cvs) {
            String cvText = buildCvText(cv);
            documents.add(preprocessText(cvText));
        }

        Map<String, Map<Integer, Double>> tfidfVectors = calculateTfIdf(documents);

        List<CvRanking> rankings = new ArrayList<>();

        for (int i = 0; i < cvs.size(); i++) {
            CvInfo cv = cvs.get(i);
            double similarity = calculateCosineSimilarity(tfidfVectors, 0, i + 1); // 0 = job desc, i+1 = CV

            CvRanking ranking = new CvRanking();
            ranking.setCvInfo(cv);
            ranking.setSimilarityScore(similarity);
            ranking.setRankedAt(LocalDateTime.now());

            rankings.add(ranking);
        }

        return rankings.stream()
                .sorted((r1, r2) -> Double.compare(r2.getSimilarityScore(), r1.getSimilarityScore()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    private String buildCvText(CvInfo cv) {
        StringBuilder cvText = new StringBuilder();

        appendIfNotNull(cvText, cv.getDescription());
        appendIfNotNull(cvText, cv.getSkills());
        appendIfNotNull(cvText, cv.getExperience());
        appendIfNotNull(cvText, cv.getEducation());
        appendIfNotNull(cvText, cv.getName());

        return cvText.toString();
    }

    private void appendIfNotNull(StringBuilder sb, String text) {
        if (text != null && !text.trim().isEmpty()) {
            sb.append(" ").append(text);
        }
    }

    private String preprocessText(String text) {
        if (text == null) return "";

        return Arrays.stream(text.toLowerCase()
                        .replaceAll("[^a-zA-ZÀ-ÿ0-9\\s]", " ")
                        .replaceAll("\\s+", " ")
                        .trim()
                        .split("\\s+"))
                .filter(word -> word.length() > 2 && !STOPWORDS.contains(word))
                .collect(Collectors.joining(" "));
    }


    private Map<String, Map<Integer, Double>> calculateTfIdf(List<String> documents) {
        // Calculate term frequencies
        List<Map<String, Integer>> termFreqs = new ArrayList<>();
        Set<String> vocabulary = new HashSet<>();

        for (String doc : documents) {
            Map<String, Integer> tf = new HashMap<>();
            String[] words = doc.split("\\s+");

            for (String word : words) {
                if (!word.isEmpty()) {
                    tf.put(word, tf.getOrDefault(word, 0) + 1);
                    vocabulary.add(word);
                }
            }
            termFreqs.add(tf);
        }

        Map<String, Integer> docFreqs = new HashMap<>();
        for (String term : vocabulary) {
            int docCount = 0;
            for (Map<String, Integer> tf : termFreqs) {
                if (tf.containsKey(term)) {
                    docCount++;
                }
            }
            docFreqs.put(term, docCount);
        }

        Map<String, Map<Integer, Double>> tfidfVectors = new HashMap<>();

        for (String term : vocabulary) {
            Map<Integer, Double> termVector = new HashMap<>();

            for (int docIndex = 0; docIndex < documents.size(); docIndex++) {
                Map<String, Integer> tf = termFreqs.get(docIndex);
                int termFreq = tf.getOrDefault(term, 0);

                if (termFreq > 0) {
                    double tfidf = (1.0 + Math.log(termFreq)) *
                            Math.log((double) documents.size() / docFreqs.get(term));
                    termVector.put(docIndex, tfidf);
                }
            }

            if (!termVector.isEmpty()) {
                tfidfVectors.put(term, termVector);
            }
        }

        return tfidfVectors;
    }

    private double calculateCosineSimilarity(Map<String, Map<Integer, Double>> tfidfVectors,
                                             int doc1Index, int doc2Index) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (Map<Integer, Double> termVector : tfidfVectors.values()) {
            Double val1 = termVector.get(doc1Index);
            Double val2 = termVector.get(doc2Index);

            if (val1 != null && val2 != null) {
                dotProduct += val1 * val2;
            }

            if (val1 != null) {
                norm1 += val1 * val1;
            }

            if (val2 != null) {
                norm2 += val2 * val2;
            }
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}