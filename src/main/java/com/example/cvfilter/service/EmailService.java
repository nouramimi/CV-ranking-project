package com.example.cvfilter.service;

import com.example.cvfilter.dao.entity.CvRanking;
import com.example.cvfilter.service.impl.EmailServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.List;

@Service
public class EmailService implements EmailServiceInterface {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.company.name:Notre Entreprise}")
    private String companyName;

    @Override
    public void sendAcceptanceEmails(List<CvRanking> acceptedCandidates, Long jobOfferId) {
        for (CvRanking ranking : acceptedCandidates) {
            try {
                sendAcceptanceEmail(ranking, jobOfferId);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de l'email à " +
                        ranking.getCvInfo().getEmail() + ": " + e.getMessage());
            }
        }
    }

    private void sendAcceptanceEmail(CvRanking ranking, Long jobOfferId) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(ranking.getCvInfo().getEmail());
        helper.setSubject("Félicitations ! Votre candidature a été retenue");

        String htmlContent = buildAcceptanceEmailContent(ranking, jobOfferId);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        System.out.println("Email d'acceptation envoyé à: " + ranking.getCvInfo().getEmail());
    }

    private String buildAcceptanceEmailContent(CvRanking ranking, Long jobOfferId) {
        return """
            <!DOCTYPE html>
            <html>
            ...
            """.formatted(
                ranking.getCvInfo().getName(),
                jobOfferId,
                ranking.getRank(),
                5,
                ranking.getSimilarityPercentage(),
                companyName
        );
    }

    @Override
    public void sendSimpleAcceptanceEmail(CvRanking ranking, Long jobOfferId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ranking.getCvInfo().getEmail());
            message.setSubject("Candidature retenue - Offre #" + jobOfferId);

            String content = """
                Cher(e) %s,
                ...
                """.formatted(
                    ranking.getCvInfo().getName(),
                    jobOfferId,
                    ranking.getRank(),
                    ranking.getSimilarityPercentage()
            );

            message.setText(content);
            mailSender.send(message);
            System.out.println("Email simple envoyé à: " + ranking.getCvInfo().getEmail());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email simple: " + e.getMessage());
        }
    }
}
