package com.common_wealth_builders.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummaryResponse {
    
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private BigDecimal pendingPayments;
    private BigDecimal verifiedPayments;
    private BigDecimal pendingExpenses;
    private BigDecimal approvedExpenses;
    private Integer totalPaymentCount;
    private Integer totalExpenseCount;
    private Map<String, BigDecimal> expensesByCategory;
    private LocalDateTime reportGeneratedAt;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}