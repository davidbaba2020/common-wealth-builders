package com.common_wealth_builders.service;

import com.common_wealth_builders.dto.request.ApproveExpenseRequest;
import com.common_wealth_builders.dto.request.ExpenseRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import org.springframework.data.domain.Pageable;

public interface ExpenseService {
    GenericResponse createExpense(ExpenseRequest request, String createdBy);
    GenericResponse getAllExpenses(Pageable pageable);
    GenericResponse getExpenseById(Long id);
    GenericResponse updateExpense(Long id, ExpenseRequest request, String updatedBy);
    GenericResponse deleteExpense(Long id, String deletedBy);
    GenericResponse approveExpense(Long id, ApproveExpenseRequest request, String approvedBy);
    GenericResponse getPendingExpenses(Pageable pageable);
    GenericResponse searchExpenses(String category, Boolean isApproved, String search, Pageable pageable);
}