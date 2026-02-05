package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.ReportFilterRequest;
import com.common_wealth_builders.dto.request.SendReportRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.ReportService;
import com.common_wealth_builders.service.impl.EmailService;
import com.common_wealth_builders.service.impl.PdfReportService;
import com.common_wealth_builders.service.impl.WhatsAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Financial reporting and analytics endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {
    
    private final ReportService reportService;
    private final PdfReportService pdfReportService;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;
    
    @Operation(
            summary = "Generate financial summary report",
            description = "Generates a comprehensive financial summary including income, expenses, and net balance for the specified period"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping("/financial-summary")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generateFinancialSummary(
            @Valid @RequestBody ReportFilterRequest request) {
        
        log.info("Request received: POST /reports/financial-summary - startDate={}, endDate={}", 
                request.getStartDate(), request.getEndDate());
        
        GenericResponse response = reportService.generateFinancialSummary(request);
        
        log.info("Response sent: POST /reports/financial-summary - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @Operation(
            summary = "Download financial summary as PDF",
            description = "Downloads the financial summary report in PDF format"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generated successfully",
                    content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/financial-summary/download")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<byte[]> downloadFinancialSummaryPdf(
            @Valid @RequestBody ReportFilterRequest request) {
        
        log.info("Request received: POST /reports/financial-summary/download");
        
        GenericResponse reportResponse = reportService.generateFinancialSummary(request);
        Map<String, Object> reportData = (Map<String, Object>) reportResponse.getData();
        
        byte[] pdfBytes = pdfReportService.generateFinancialSummaryPdf(reportData);
        
        String filename = "Financial_Summary_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        log.info("PDF generated successfully: {}", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @Operation(
            summary = "Send financial summary via email",
            description = "Generates and sends the financial summary report as PDF via email"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email address"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/financial-summary/send-email")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> sendFinancialSummaryEmail(
            @Valid @RequestBody ReportFilterRequest reportRequest,
            @Valid @RequestBody SendReportRequest sendRequest) {
        
        log.info("Request received: POST /reports/financial-summary/send-email to {}", 
                sendRequest.getEmail());
        
        GenericResponse reportResponse = reportService.generateFinancialSummary(reportRequest);
        Map<String, Object> reportData = (Map<String, Object>) reportResponse.getData();
        
        byte[] pdfBytes = pdfReportService.generateFinancialSummaryPdf(reportData);
        
        String subject = sendRequest.getSubject() != null ? 
                sendRequest.getSubject() : "Financial Summary Report";
        
        emailService.sendReportEmail(
                sendRequest.getEmail(),
                subject,
                "Financial Summary",
                pdfBytes,
                sendRequest.getRecipientName()
        );
        
        log.info("Email sent successfully to: {}", sendRequest.getEmail());
        
        return ResponseEntity.ok(GenericResponse.builder()
                .isSuccess(true)
                .message("Financial summary report sent successfully via email")
                .build());
    }

    @Operation(
            summary = "Send financial summary via WhatsApp",
            description = "Sends a WhatsApp notification with download link for the financial summary report"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "WhatsApp message sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid phone number"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/financial-summary/send-whatsapp")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> sendFinancialSummaryWhatsApp(
            @Valid @RequestBody SendReportRequest sendRequest) {
        
        log.info("Request received: POST /reports/financial-summary/send-whatsapp to {}", 
                sendRequest.getPhoneNumber());
        
        // In production, you would generate a secure download link
        String downloadUrl = "https://app.commonwealthbuilders.com/downloads/report-" + 
                System.currentTimeMillis();
        
        whatsAppService.sendReportNotification(
                sendRequest.getPhoneNumber(),
                "Financial Summary Report",
                downloadUrl
        );
        
        log.info("WhatsApp sent successfully to: {}", sendRequest.getPhoneNumber());
        
        return ResponseEntity.ok(GenericResponse.builder()
                .isSuccess(true)
                .message("Financial summary report link sent successfully via WhatsApp")
                .build());
    }
    
    @Operation(
            summary = "Generate user contribution report",
            description = "Generates a detailed contribution report for a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/user-contribution/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generateUserContributionReport(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId) {
        
        log.info("Request received: GET /reports/user-contribution/{} - userId={}", userId, userId);
        
        GenericResponse response = reportService.generateUserContributionReport(userId);
        
        log.info("Response sent: GET /reports/user-contribution/{} - status={}, success={}", 
                userId, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @Operation(
            summary = "Download user contribution report as PDF",
            description = "Downloads the user contribution report in PDF format"
    )
    @GetMapping("/user-contribution/{userId}/download")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<byte[]> downloadUserContributionPdf(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId) {
        
        log.info("Request received: GET /reports/user-contribution/{}/download", userId);
        
        GenericResponse reportResponse = reportService.generateUserContributionReport(userId);
        Map<String, Object> reportData = (Map<String, Object>) reportResponse.getData();
        
        byte[] pdfBytes = pdfReportService.generateUserContributionPdf(reportData);
        
        String filename = "User_Contribution_" + userId + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    @Operation(
            summary = "Generate expense report",
            description = "Generates a detailed expense report for the specified period"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/expenses")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generateExpenseReport(
            @Valid @RequestBody ReportFilterRequest request) {
        
        log.info("Request received: POST /reports/expenses - startDate={}, endDate={}", 
                request.getStartDate(), request.getEndDate());
        
        GenericResponse response = reportService.generateExpenseReport(request);
        
        log.info("Response sent: POST /reports/expenses - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @Operation(
            summary = "Download expense report as PDF",
            description = "Downloads the expense report in PDF format"
    )
    @PostMapping("/expenses/download")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<byte[]> downloadExpenseReportPdf(
            @Valid @RequestBody ReportFilterRequest request) {
        
        log.info("Request received: POST /reports/expenses/download");
        
        GenericResponse reportResponse = reportService.generateExpenseReport(request);
        Map<String, Object> reportData = (Map<String, Object>) reportResponse.getData();
        
        byte[] pdfBytes = pdfReportService.generateExpenseReportPdf(reportData);
        
        String filename = "Expense_Report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    @Operation(
            summary = "Generate payment report",
            description = "Generates a comprehensive payment report for the specified period"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generatePaymentReport(
            @Valid @RequestBody ReportFilterRequest request) {
        
        log.info("Request received: POST /reports/payments - startDate={}, endDate={}", 
                request.getStartDate(), request.getEndDate());
        
        GenericResponse response = reportService.generatePaymentReport(request);
        
        log.info("Response sent: POST /reports/payments - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @Operation(
            summary = "Download payment report as PDF",
            description = "Downloads the payment report in PDF format"
    )
    @PostMapping("/payments/download")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<byte[]> downloadPaymentReportPdf(
            @Valid @RequestBody ReportFilterRequest request) {
        
        log.info("Request received: POST /reports/payments/download");
        
        GenericResponse reportResponse = reportService.generatePaymentReport(request);
        Map<String, Object> reportData = (Map<String, Object>) reportResponse.getData();
        
        byte[] pdfBytes = pdfReportService.generatePaymentReportPdf(reportData);
        
        String filename = "Payment_Report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    
    @Operation(
            summary = "Generate monthly report",
            description = "Generates a comprehensive monthly summary report"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid year or month"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generateMonthlyReport(
            @Parameter(description = "Year", example = "2026")
            @RequestParam int year,
            @Parameter(description = "Month (1-12)", example = "2")
            @RequestParam int month) {
        
        log.info("Request received: GET /reports/monthly - year={}, month={}", year, month);
        
        GenericResponse response = reportService.generateMonthlyReport(year, month);
        
        log.info("Response sent: GET /reports/monthly - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}