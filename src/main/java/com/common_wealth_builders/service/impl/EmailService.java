package com.common_wealth_builders.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for sending emails with attachments
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${application.name:Commonwealth Builders}")
    private String applicationName;

    /**
     * Send email with PDF attachment
     */
    public void sendReportEmail(String toEmail, String subject, String reportType, 
                                byte[] pdfContent, String recipientName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, applicationName);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            // Create email body using Thymeleaf template
            Context context = new Context();
            context.setVariable("recipientName", recipientName);
            context.setVariable("reportType", reportType);
            context.setVariable("generatedDate", LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")));
            context.setVariable("applicationName", applicationName);

            String htmlContent = templateEngine.process("email/report-email", context);
            helper.setText(htmlContent, true);

            // Attach PDF
            String filename = reportType.replaceAll("\\s+", "_") + "_" + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            helper.addAttachment(filename, new ByteArrayResource(pdfContent));

            mailSender.send(message);
            log.info("Report email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send report email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Send bulk report emails
     */
    public void sendBulkReportEmails(String[] toEmails, String subject, String reportType, 
                                     byte[] pdfContent) {
        for (String email : toEmails) {
            try {
                sendReportEmail(email, subject, reportType, pdfContent, "Admin");
            } catch (Exception e) {
                log.error("Failed to send email to: {}", email, e);
                // Continue with other emails
            }
        }
    }

    /**
     * Send simple notification email
     */
    public void sendNotificationEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, applicationName);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("content", content);
            context.setVariable("applicationName", applicationName);

            String htmlContent = templateEngine.process("email/notification-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Notification email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send notification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send notification: " + e.getMessage(), e);
        }
    }
}