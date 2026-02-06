package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.ReportFilterRequest;
import com.common_wealth_builders.dto.request.SendReportRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.impl.EmailService;
import com.common_wealth_builders.service.impl.PdfReportService;
import com.common_wealth_builders.service.ReportService;
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
@Tag(name = "Financial Reports", description = "Financial reporting - FIN_ADMIN generates and distributes reports")
@SecurityRequirement(name = "Bearer Authentication")
public class ReportController {
    
    private final ReportService reportService;
    private final PdfReportService pdfReportService;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;
    
    @Operation(
            summary = "Generate financial summary (FIN ADMIN)",
            description = "Generates financial summary report. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires FIN_ADMIN role")
    })
    @PostMapping("/financial-summary")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generateFinancialSummary(
            @Valid @RequestBody ReportFilterRequest request) {
        
        log.info("Request received: POST /reports/financial-summary - startDate={}, endDate={}", 
                request.getStartDate(), request.getEndDate());
        
        GenericResponse response = reportService.generateFinancialSummary(request);
        
        log.info("Response sent: POST /reports/financial-summary - status={}", response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @Operation(
            summary = "Download financial summary PDF (FIN ADMIN)",
            description = "Downloads financial summary as PDF. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @PostMapping("/financial-summary/download")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
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
        
        log.info("PDF generated: {}", filename);
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @Operation(
            summary = "Send financial summary via email (FIN ADMIN)",
            description = "Emails financial summary PDF. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @PostMapping("/financial-summary/send-email")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> sendFinancialSummaryEmail(
            @Valid @RequestBody ReportFilterRequest reportRequest,
            @Valid @RequestBody SendReportRequest sendRequest) {
        
        log.info("Sending financial summary to {}", sendRequest.getEmail());
        
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
        
        return ResponseEntity.ok(GenericResponse.builder()
                .isSuccess(true)
                .message("Financial summary sent successfully via email")
                .build());
    }

    @Operation(
            summary = "Send financial summary via WhatsApp (FIN ADMIN)",
            description = "Sends WhatsApp notification with report link. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @PostMapping("/financial-summary/send-whatsapp")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> sendFinancialSummaryWhatsApp(
            @Valid @RequestBody SendReportRequest sendRequest) {
        
        log.info("Sending WhatsApp to {}", sendRequest.getPhoneNumber());
        
        String downloadUrl = "https://app.commonwealthbuilders.com/downloads/report-" + 
                System.currentTimeMillis();
        
        whatsAppService.sendReportNotification(
                sendRequest.getPhoneNumber(),
                "Financial Summary Report",
                downloadUrl
        );
        
        return ResponseEntity.ok(GenericResponse.builder()
                .isSuccess(true)
                .message("Report link sent via WhatsApp")
                .build());
    }
    
    @Operation(
            summary = "Generate user contribution report (FIN ADMIN)",
            description = "Generates user contribution report. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @GetMapping("/user-contribution/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generateUserContributionReport(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long userId) {
        
        log.info("Request received: GET /reports/user-contribution/{}", userId);
        
        GenericResponse response = reportService.generateUserContributionReport(userId);
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @Operation(
            summary = "Download user contribution PDF (FIN ADMIN)",
            description = "Downloads user contribution as PDF. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @GetMapping("/user-contribution/{userId}/download")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<byte[]> downloadUserContributionPdf(
            @PathVariable Long userId) {
        
        GenericResponse reportResponse = reportService.generateUserContributionReport(userId);
        Map<String, Object> reportData = (Map<String, Object>) reportResponse.getData();
        
        byte[] pdfBytes = pdfReportService.generateUserContributionPdf(reportData);
        
        String filename = "User_Contribution_" + userId + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
    
    @Operation(
            summary = "Generate expense report (FIN ADMIN)",
            description = "Generates expense report. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @PostMapping("/expenses")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generateExpenseReport(
            @Valid @RequestBody ReportFilterRequest request) {
        
        log.info("Request received: POST /reports/expenses");
        
        GenericResponse response = reportService.generateExpenseReport(request);
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @Operation(
            summary = "Download expense report PDF (FIN ADMIN)",
            description = "Downloads expense report as PDF. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @PostMapping("/expenses/download")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<byte[]> downloadExpenseReportPdf(
            @Valid @RequestBody ReportFilterRequest request) {
        
        GenericResponse reportResponse = reportService.generateExpenseReport(request);
        Map<String, Object> reportData = (Map<String, Object>) reportResponse.getData();
        
        byte[] pdfBytes = pdfReportService.generateExpenseReportPdf(reportData);
        
        String filename = "Expense_Report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
    
    @Operation(
            summary = "Generate payment report (FIN ADMIN)",
            description = "Generates payment report. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generatePaymentReport(
            @Valid @RequestBody ReportFilterRequest request) {
        
        log.info("Request received: POST /reports/payments");
        
        GenericResponse response = reportService.generatePaymentReport(request);
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @Operation(
            summary = "Download payment report PDF (FIN ADMIN)",
            description = "Downloads payment report as PDF. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @PostMapping("/payments/download")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<byte[]> downloadPaymentReportPdf(
            @Valid @RequestBody ReportFilterRequest request) {
        
        GenericResponse reportResponse = reportService.generatePaymentReport(request);
        Map<String, Object> reportData = (Map<String, Object>) reportResponse.getData();
        
        byte[] pdfBytes = pdfReportService.generatePaymentReportPdf(reportData);
        
        String filename = "Payment_Report_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
    
    @Operation(
            summary = "Generate monthly report (FIN ADMIN)",
            description = "Generates monthly summary. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generateMonthlyReport(
            @Parameter(description = "Year", example = "2026")
            @RequestParam int year,
            
            @Parameter(description = "Month (1-12)", example = "2")
            @RequestParam int month) {
        
        log.info("Request received: GET /reports/monthly - year={}, month={}", year, month);
        
        GenericResponse response = reportService.generateMonthlyReport(year, month);
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}