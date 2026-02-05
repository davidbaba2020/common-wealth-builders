package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.ReportFilterRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    
    private final ReportService reportService;
    
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
    
    @GetMapping("/user-contribution/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generateUserContributionReport(@PathVariable Long userId) {
        log.info("Request received: GET /reports/user-contribution/{} - userId={}", userId, userId);
        
        GenericResponse response = reportService.generateUserContributionReport(userId);
        
        log.info("Response sent: GET /reports/user-contribution/{} - status={}, success={}", 
                userId, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
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
    
    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> generateMonthlyReport(
            @RequestParam int year,
            @RequestParam int month) {
        
        log.info("Request received: GET /reports/monthly - year={}, month={}", year, month);
        
        GenericResponse response = reportService.generateMonthlyReport(year, month);
        
        log.info("Response sent: GET /reports/monthly - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}