package com.common_wealth_builders.service.impl;

import com.common_wealth_builders.dto.request.ReportFilterRequest;
import com.common_wealth_builders.dto.response.FinancialSummaryResponse;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.dto.response.UserContributionResponse;
import com.common_wealth_builders.entity.Payment;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.exception.ResourceNotFoundException;
import com.common_wealth_builders.repository.ExpenseRepository;
import com.common_wealth_builders.repository.PaymentRepository;
import com.common_wealth_builders.repository.UserRepository;
import com.common_wealth_builders.service.AuditService;
import com.common_wealth_builders.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {
    
    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    
    @Override
    @Transactional(readOnly = true)
    public GenericResponse generateFinancialSummary(ReportFilterRequest request) {
        log.info("Generating financial summary: startDate={}, endDate={}", 
                request.getStartDate(), request.getEndDate());
        
        LocalDateTime startDate = request.getStartDate() != null ? 
                request.getStartDate() : LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = request.getEndDate() != null ? 
                request.getEndDate() : LocalDateTime.now();
        
        BigDecimal totalIncome = paymentRepository.sumVerifiedPaymentsBetween(startDate, endDate);
        BigDecimal totalExpenses = expenseRepository.sumApprovedExpensesBetween(startDate, endDate);
        
        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);
        
        List<Payment> allPayments = paymentRepository.findByPaymentDateBetween(startDate, endDate);
        BigDecimal pendingPayments = allPayments.stream()
                .filter(p -> !p.isVerified())
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<Object[]> expensesByCategory = expenseRepository.sumExpensesByCategory(startDate, endDate);
        Map<String, BigDecimal> categoryMap = new HashMap<>();
        for (Object[] result : expensesByCategory) {
            categoryMap.put(result[0].toString(), (BigDecimal) result[1]);
        }
        
        FinancialSummaryResponse summary = FinancialSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .pendingPayments(pendingPayments)
                .verifiedPayments(totalIncome)
                .approvedExpenses(totalExpenses)
                .totalPaymentCount(allPayments.size())
                .expensesByCategory(categoryMap)
                .reportGeneratedAt(LocalDateTime.now())
                .periodStart(startDate)
                .periodEnd(endDate)
                .build();
        
        log.info("Financial summary generated: totalIncome={}, totalExpenses={}, netBalance={}", 
                totalIncome, totalExpenses, netBalance);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Financial summary generated successfully")
                .data(summary)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public GenericResponse generateUserContributionReport(Long userId) {
        log.info("Generating contribution report for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        BigDecimal totalContributions = paymentRepository.sumVerifiedPaymentsByUserId(userId);
        totalContributions = totalContributions != null ? totalContributions : BigDecimal.ZERO;
        
        List<Payment> userPayments = paymentRepository.findByUserIdAndIsVerifiedTrue(userId);
        BigDecimal verifiedContributions = userPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        LocalDateTime lastPaymentDate = userPayments.stream()
                .map(Payment::getPaymentDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        
        LocalDateTime firstPaymentDate = userPayments.stream()
                .map(Payment::getPaymentDate)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        
        UserContributionResponse report = UserContributionResponse.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .userFullName(user.getFirstname() + " " + user.getLastname())
                .totalContributions(totalContributions)
                .verifiedContributions(verifiedContributions)
                .pendingContributions(BigDecimal.ZERO)
                .paymentCount(userPayments.size())
                .lastPaymentDate(lastPaymentDate)
                .firstPaymentDate(firstPaymentDate)
                .build();
        
        log.info("User contribution report generated: userId={}, totalContributions={}", 
                userId, totalContributions);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("User contribution report generated successfully")
                .data(report)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public GenericResponse generateExpenseReport(ReportFilterRequest request) {
        log.info("Generating expense report: startDate={}, endDate={}", 
                request.getStartDate(), request.getEndDate());
        
        LocalDateTime startDate = request.getStartDate() != null ? 
                request.getStartDate() : LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = request.getEndDate() != null ? 
                request.getEndDate() : LocalDateTime.now();
        
        BigDecimal totalExpenses = expenseRepository.sumApprovedExpensesBetween(startDate, endDate);
        totalExpenses = totalExpenses != null ? totalExpenses : BigDecimal.ZERO;
        
        List<Object[]> expensesByCategory = expenseRepository.sumExpensesByCategory(startDate, endDate);
        Map<String, BigDecimal> categoryMap = new HashMap<>();
        for (Object[] result : expensesByCategory) {
            categoryMap.put(result[0].toString(), (BigDecimal) result[1]);
        }
        
        Map<String, Object> expenseReport = new HashMap<>();
        expenseReport.put("totalExpenses", totalExpenses);
        expenseReport.put("expensesByCategory", categoryMap);
        expenseReport.put("periodStart", startDate);
        expenseReport.put("periodEnd", endDate);
        expenseReport.put("reportGeneratedAt", LocalDateTime.now());
        
        log.info("Expense report generated: totalExpenses={}", totalExpenses);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Expense report generated successfully")
                .data(expenseReport)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public GenericResponse generatePaymentReport(ReportFilterRequest request) {
        log.info("Generating payment report: startDate={}, endDate={}", 
                request.getStartDate(), request.getEndDate());
        
        LocalDateTime startDate = request.getStartDate() != null ? 
                request.getStartDate() : LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = request.getEndDate() != null ? 
                request.getEndDate() : LocalDateTime.now();
        
        List<Payment> payments = paymentRepository.findByPaymentDateBetween(startDate, endDate);
        
        BigDecimal totalPayments = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal verifiedPayments = payments.stream()
                .filter(Payment::isVerified)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal pendingPayments = payments.stream()
                .filter(p -> !p.isVerified())
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Object> paymentReport = new HashMap<>();
        paymentReport.put("totalPayments", totalPayments);
        paymentReport.put("verifiedPayments", verifiedPayments);
        paymentReport.put("pendingPayments", pendingPayments);
        paymentReport.put("totalCount", payments.size());
        paymentReport.put("verifiedCount", payments.stream().filter(Payment::isVerified).count());
        paymentReport.put("pendingCount", payments.stream().filter(p -> !p.isVerified()).count());
        paymentReport.put("periodStart", startDate);
        paymentReport.put("periodEnd", endDate);
        paymentReport.put("reportGeneratedAt", LocalDateTime.now());
        
        log.info("Payment report generated: totalPayments={}, verifiedPayments={}", 
                totalPayments, verifiedPayments);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Payment report generated successfully")
                .data(paymentReport)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public GenericResponse generateMonthlyReport(int year, int month) {
        log.info("Generating monthly report: year={}, month={}", year, month);
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        
        ReportFilterRequest request = ReportFilterRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        
        GenericResponse financialSummary = generateFinancialSummary(request);
        
        log.info("Monthly report generated for: {}-{}", year, month);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Monthly report generated successfully")
                .data(financialSummary.getData())
                .httpStatus(HttpStatus.OK)
                .build();
    }
}