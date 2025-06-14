package com.example.cvfilter.util;

import com.example.cvfilter.model.CvInfo;
import com.example.cvfilter.model.CvRanking;
import com.example.cvfilter.model.JobOffer;
import com.example.cvfilter.service.CvRankingService;
import com.example.cvfilter.service.JobOfferService;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CvRankingTestUtil {

    private final CvRankingService cvRankingService;
    private final JobOfferService jobOfferService;

    public CvRankingTestUtil(CvRankingService cvRankingService, JobOfferService jobOfferService) {
        this.cvRankingService = cvRankingService;
        this.jobOfferService = jobOfferService;
    }

    public void testCvRanking(Long jobOfferId) {
        System.out.println("=== CV RANKING TEST FOR JOB OFFER " + jobOfferId + " ===");

        try {
            // Get job offer details
            JobOffer jobOffer = jobOfferService.getById(jobOfferId).orElse(null);
            if (jobOffer == null) {
                System.out.println("❌ Job offer not found: " + jobOfferId);
                return;
            }

            System.out.println("📄 Job Description: " +
                    (jobOffer.getDescription().length() > 100 ?
                            jobOffer.getDescription().substring(0, 100) + "..." :
                            jobOffer.getDescription()));

            // Get CV rankings
            List<CvRanking> rankings = cvRankingService.getBestCvsForJob(jobOfferId);

            if (rankings.isEmpty()) {
                System.out.println("❌ No CVs found for this job offer");
                return;
            }

            System.out.println("✅ Found " + rankings.size() + " CVs");
            System.out.println("\n📊 RANKING RESULTS:");
            System.out.println("Rank | User ID | Similarity | Name                 | Skills Preview");
            System.out.println("-----|---------|------------|----------------------|--------------------------");

            for (int i = 0; i < rankings.size(); i++) {
                CvRanking ranking = rankings.get(i);
                CvInfo cv = ranking.getCvInfo();

                String skillsPreview = cv.getSkills() != null ?
                        (cv.getSkills().length() > 50 ? cv.getSkills().substring(0, 50) + "..." : cv.getSkills()) :
                        "N/A";

                System.out.printf("%4d | %7d | %9.2f%% | %-20s | %s%n",
                        i + 1,
                        cv.getUserId(),
                        ranking.getSimilarityPercentage(),
                        cv.getName() != null ? cv.getName() : "N/A",
                        skillsPreview);
            }

            // Print statistics
            double maxScore = rankings.stream().mapToDouble(CvRanking::getSimilarityScore).max().orElse(0);
            double avgScore = rankings.stream().mapToDouble(CvRanking::getSimilarityScore).average().orElse(0);
            double minScore = rankings.stream().mapToDouble(CvRanking::getSimilarityScore).min().orElse(0);

            System.out.println("\n📈 STATISTICS:");
            System.out.printf("Highest Score: %.4f%n", maxScore);
            System.out.printf("Average Score: %.4f%n", avgScore);
            System.out.printf("Lowest Score : %.4f%n", minScore);

            // Optionally write results to a file
            saveResultsToFile(jobOfferId, rankings);

        } catch (Exception e) {
            System.err.println("❌ Error during CV ranking test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveResultsToFile(Long jobOfferId, List<CvRanking> rankings) {
        String filename = "cv_ranking_job_" + jobOfferId + "_" + LocalDateTime.now().toString().replace(":", "-") + ".txt";

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("CV Ranking for Job Offer ID: " + jobOfferId + "\n");
            writer.write("Generated at: " + LocalDateTime.now() + "\n\n");

            writer.write("Rank | User ID | Similarity | Name                 | Skills Preview\n");
            writer.write("-----|---------|------------|----------------------|--------------------------\n");

            for (int i = 0; i < rankings.size(); i++) {
                CvRanking ranking = rankings.get(i);
                CvInfo cv = ranking.getCvInfo();

                String skillsPreview = cv.getSkills() != null ?
                        (cv.getSkills().length() > 50 ? cv.getSkills().substring(0, 50) + "..." : cv.getSkills()) :
                        "N/A";

                writer.write(String.format("%4d | %7d | %9.2f%% | %-20s | %s%n",
                        i + 1,
                        cv.getUserId(),
                        ranking.getSimilarityPercentage(),
                        cv.getName() != null ? cv.getName() : "N/A",
                        skillsPreview));
            }

            System.out.println("📁 Results saved to file: " + filename);
        } catch (IOException e) {
            System.err.println("❌ Failed to save results to file: " + e.getMessage());
        }
    }
}
