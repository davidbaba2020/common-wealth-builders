package com.common_wealth_builders.service.impl;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for sending WhatsApp messages via Twilio
 */
@Service
@Slf4j
public class WhatsAppService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.whatsapp-number}")
    private String whatsAppNumber;

    @PostConstruct
    public void init() {
        if (accountSid != null && authToken != null) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio WhatsApp service initialized successfully");
        } else {
            log.warn("Twilio credentials not configured. WhatsApp service will not be available.");
        }
    }

    /**
     * Send WhatsApp message
     */
    public void sendWhatsAppMessage(String toPhoneNumber, String messageContent) {
        try {
            if (accountSid == null || authToken == null) {
                throw new RuntimeException("Twilio credentials not configured");
            }

            // Ensure phone number is in E.164 format (e.g., +2348012345678)
            if (!toPhoneNumber.startsWith("+")) {
                toPhoneNumber = "+" + toPhoneNumber;
            }

            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + toPhoneNumber),
                    new PhoneNumber("whatsapp:" + whatsAppNumber),
                    messageContent
            ).create();

            log.info("WhatsApp message sent successfully. SID: {}, To: {}", 
                    message.getSid(), toPhoneNumber);

        } catch (Exception e) {
            log.error("Failed to send WhatsApp message to: {}", toPhoneNumber, e);
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage(), e);
        }
    }

    /**
     * Send report notification via WhatsApp
     */
    public void sendReportNotification(String toPhoneNumber, String reportType, String downloadUrl) {
        String message = String.format("""
                *Commonwealth Builders - Report Generated*
                
                Report Type: %s
                Generated: %s
                
                Your report has been generated successfully!
                
                Download Link: %s
                
                This link will expire in 24 hours.
                
                For support, contact: support@commonwealthbuilders.com
                """,
                reportType,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                downloadUrl
        );

        sendWhatsAppMessage(toPhoneNumber, message);
    }

    /**
     * Send bulk WhatsApp notifications
     */
    public void sendBulkNotifications(String[] phoneNumbers, String reportType, String downloadUrl) {
        for (String phoneNumber : phoneNumbers) {
            try {
                sendReportNotification(phoneNumber, reportType, downloadUrl);
            } catch (Exception e) {
                log.error("Failed to send WhatsApp to: {}", phoneNumber, e);
                // Continue with other numbers
            }
        }
    }

    /**
     * Send simple WhatsApp notification
     */
    public void sendSimpleNotification(String toPhoneNumber, String title, String content) {
        String message = String.format("""
                *%s*
                
                %s
                
                ---
                Commonwealth Builders
                """,
                title,
                content
        );

        sendWhatsAppMessage(toPhoneNumber, message);
    }
}