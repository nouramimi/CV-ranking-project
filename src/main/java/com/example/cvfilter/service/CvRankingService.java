package com.example.cvfilter.service;

import com.example.cvfilter.model.CvInfo;
import com.example.cvfilter.model.CvRanking;
import com.example.cvfilter.model.JobOffer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CvRankingService {

    @Value("${cv.extracted.info.file:cv_extracted_info.csv}")
    private String extractedInfoFile;

    private final JobOfferService jobOfferService;

    private static final Set<String> STOPWORDS = Set.of(
            "le", "de", "et", "à", "un", "il", "être", "en", "avoir", "que", "pour",
            "dans", "ce", "son", "une", "sur", "avec", "ne", "se", "pas", "tout", "plus",
            "par", "grand", "the", "be", "to", "of", "and", "a", "in", "that", "have",
            "i", "it", "for", "not", "on", "with", "he", "as", "you", "do", "at"
    );

    public CvRankingService(JobOfferService jobOfferService) {
        this.jobOfferService = jobOfferService;
    }

    public List<CvRanking> getTopCvsForJob(Long jobOfferId, int topN) {
        try {
            Optional<JobOffer> jobOfferOpt = jobOfferService.getById(jobOfferId);
            if (jobOfferOpt.isEmpty()) {
                throw new IllegalArgumentException("Job offer not found: " + jobOfferId);
            }

            String jobDescription = jobOfferOpt.get().getDescription();
            if (jobDescription == null || jobDescription.trim().isEmpty()) {
                throw new IllegalArgumentException("Job offer has no description");
            }

            List<CvInfo> cvs = loadCvsForJobOffer(jobOfferId);
            if (cvs.isEmpty()) {
                return Collections.emptyList();
            }

            return rankCvs(jobDescription, cvs, Math.min(topN, cvs.size()));

        } catch (Exception e) {
            System.err.println("Error ranking CVs for job " + jobOfferId + ": " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<CvRanking> getBestCvsForJob(Long jobOfferId) {
        return getTopCvsForJob(jobOfferId, 5);
    }

    private List<CvInfo> loadCvsForJobOffer(Long jobOfferId) throws IOException {
        List<CvInfo> cvs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(extractedInfoFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                CvInfo cvInfo = parseCsvLine(line);
                if (cvInfo != null && jobOfferId.equals(cvInfo.getJobOfferId())) {
                    cvs.add(cvInfo);
                }
            }
        }

        System.out.println("Loaded " + cvs.size() + " CVs for job offer " + jobOfferId);
        return cvs;
    }

    private CvInfo parseCsvLine(String line) {
        try {
            List<String> fields = parseCsvFields(line);

            if (fields.size() < 11) {
                return null;
            }

            Long userId = Long.parseLong(fields.get(0));
            Long jobOfferId = Long.parseLong(fields.get(1));
            String cvPath = fields.get(2);

            CvInfo cvInfo = new CvInfo(userId, jobOfferId, cvPath);
            cvInfo.setName(fields.get(3));
            cvInfo.setEmail(fields.get(4));
            cvInfo.setPhone(fields.get(5));
            cvInfo.setDescription(fields.get(6));
            cvInfo.setSkills(fields.get(7));
            cvInfo.setExperience(fields.get(8));
            cvInfo.setEducation(fields.get(9));

            String timestamp = fields.get(10);
            cvInfo.setExtractedAt(LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return cvInfo;
        } catch (Exception e) {
            System.err.println("Error parsing CSV line: " + line + " - " + e.getMessage());
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
                    // Escaped quote
                    currentField.append('"');
                    i++; // Skip next quote
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