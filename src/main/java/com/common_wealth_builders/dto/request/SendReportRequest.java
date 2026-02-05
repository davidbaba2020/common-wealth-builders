package com.common_wealth_builders.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending reports via email or WhatsApp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send report via email or WhatsApp")
public class SendReportRequest {

    @Schema(description = "Email address to send report to", example = "admin@example.com")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "WhatsApp phone number in E.164 format", example = "+2348012345678")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Schema(description = "Recipient name", example = "John Doe")
    @NotBlank(message = "Recipient name is required")
    private String recipientName;

    @Schema(description = "Subject line for email", example = "Monthly Financial Report")
    private String subject;

    @Schema(description = "Additional message content")
    private String message;
}