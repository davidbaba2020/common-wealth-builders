package com.common_wealth_builders.service;

import com.common_wealth_builders.dto.request.ReportFilterRequest;
import com.common_wealth_builders.dto.response.GenericResponse;

public interface ReportService {
    GenericResponse generateFinancialSummary(ReportFilterRequest request);
    GenericResponse generateUserContributionReport(Long userId);
    GenericResponse generateExpenseReport(ReportFilterRequest request);
    GenericResponse generatePaymentReport(ReportFilterRequest request);
    GenericResponse generateMonthlyReport(int year, int month);
}