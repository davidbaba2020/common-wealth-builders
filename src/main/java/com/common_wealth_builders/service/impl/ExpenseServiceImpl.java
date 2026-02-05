package com.common_wealth_builders.service.impl;

import com.common_wealth_builders.dto.request.ApproveExpenseRequest;
import com.common_wealth_builders.dto.request.ExpenseRequest;
import com.common_wealth_builders.dto.response.ExpenseResponse;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.dto.response.PageResponse;
import com.common_wealth_builders.entity.Expense;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.enums.ExpenseCategory;
import com.common_wealth_builders.exception.ResourceNotFoundException;
import com.common_wealth_builders.repository.ExpenseRepository;
import com.common_wealth_builders.repository.UserRepository;
import com.common_wealth_builders.service.AuditService;
import com.common_wealth_builders.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    
    @Override
    @Transactional
    public GenericResponse createExpense(ExpenseRequest request, String createdBy) {
        log.info("Creating expense: title={}, amount={}, category={}", 
                request.getTitle(), request.getAmount(), request.getCategory());
        
        Expense expense = Expense.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .amount(request.getAmount())
                .category(request.getCategory())
                .expenseDate(request.getExpenseDate())
                .vendor(request.getVendor())
                .receiptNumber(request.getReceiptNumber())
                .receiptUrl(request.getReceiptUrl())
                .isApproved(false)
                .build();
        
        Expense savedExpense = expenseRepository.save(expense);
        
        auditService.logAction(
                getCurrentUserId(createdBy),
                "EXPENSE_CREATED",
                "EXPENSES",
                "Expense created: " + savedExpense.getTitle()
        );
        
        log.info("Expense created successfully: id={}, title={}", 
                savedExpense.getId(), savedExpense.getTitle());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Expense created successfully")
                .data(mapToExpenseResponse(savedExpense))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }
    
    @Override
    public GenericResponse getAllExpenses(Pageable pageable) {
        log.info("Fetching all expenses with pagination");
        
        Page<Expense> expensesPage = expenseRepository.findAll(pageable);
        
        List<ExpenseResponse> expenseResponses = expensesPage.getContent().stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
        
        PageResponse<ExpenseResponse> pageResponse = PageResponse.<ExpenseResponse>builder()
                .content(expenseResponses)
                .pageNumber(expensesPage.getNumber())
                .pageSize(expensesPage.getSize())
                .totalElements(expensesPage.getTotalElements())
                .totalPages(expensesPage.getTotalPages())
                .last(expensesPage.isLast())
                .first(expensesPage.isFirst())
                .build();
        
        log.info("Successfully fetched {} expenses", expenseResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Expenses retrieved successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getExpenseById(Long id) {
        log.info("Fetching expense by ID: {}", id);
        
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
        
        log.info("Expense retrieved successfully: id={}", id);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Expense retrieved successfully")
                .data(mapToExpenseResponse(expense))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional
    public GenericResponse updateExpense(Long id, ExpenseRequest request, String updatedBy) {
        log.info("Updating expense: id={}, title={}", id, request.getTitle());
        
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
        
        if (expense.isApproved()) {
            throw new IllegalStateException("Cannot update approved expense");
        }
        
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setVendor(request.getVendor());
        expense.setReceiptNumber(request.getReceiptNumber());
        expense.setReceiptUrl(request.getReceiptUrl());
        
        Expense updatedExpense = expenseRepository.save(expense);
        
        auditService.logAction(
                getCurrentUserId(updatedBy),
                "EXPENSE_UPDATED",
                "EXPENSES",
                "Expense updated: " + updatedExpense.getTitle()
        );
        
        log.info("Expense updated successfully: id={}", id);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Expense updated successfully")
                .data(mapToExpenseResponse(updatedExpense))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional
    public GenericResponse deleteExpense(Long id, String deletedBy) {
        log.info("Deleting expense: id={}", id);
        
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
        
        if (expense.isApproved()) {
            throw new IllegalStateException("Cannot delete approved expense");
        }
        
        expense.softDelete(deletedBy);
        expenseRepository.save(expense);
        
        auditService.logAction(
                getCurrentUserId(deletedBy),
                "EXPENSE_DELETED",
                "EXPENSES",
                "Expense deleted: " + expense.getTitle()
        );
        
        log.info("Expense deleted successfully: id={}", id);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Expense deleted successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional
    public GenericResponse approveExpense(Long id, ApproveExpenseRequest request, String approvedBy) {
        log.info("Approving expense: id={}, approvedBy={}", id, approvedBy);
        
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
        
        User approver = userRepository.findByEmail(approvedBy)
                .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));
        
        expense.approve(approver, request.getRemarks());
        Expense approvedExpense = expenseRepository.save(expense);
        
        auditService.logAction(
                approver.getId(),
                "EXPENSE_APPROVED",
                "EXPENSES",
                "Expense approved: " + expense.getTitle() + " by " + approvedBy
        );
        
        log.info("Expense approved successfully: id={}", id);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Expense approved successfully")
                .data(mapToExpenseResponse(approvedExpense))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getPendingExpenses(Pageable pageable) {
        log.info("Fetching pending expenses");
        
        Page<Expense> expensesPage = expenseRepository.findByIsApproved(false, pageable);
        
        List<ExpenseResponse> expenseResponses = expensesPage.getContent().stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
        
        PageResponse<ExpenseResponse> pageResponse = PageResponse.<ExpenseResponse>builder()
                .content(expenseResponses)
                .pageNumber(expensesPage.getNumber())
                .pageSize(expensesPage.getSize())
                .totalElements(expensesPage.getTotalElements())
                .totalPages(expensesPage.getTotalPages())
                .last(expensesPage.isLast())
                .first(expensesPage.isFirst())
                .build();
        
        log.info("Found {} pending expenses", expenseResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Pending expenses retrieved successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse searchExpenses(String category, Boolean isApproved, String search, Pageable pageable) {
        log.info("Searching expenses: category={}, isApproved={}, search={}", category, isApproved, search);
        
        ExpenseCategory expenseCategory = category != null ? ExpenseCategory.valueOf(category.toUpperCase()) : null;
        
        Page<Expense> expensesPage = expenseRepository.searchExpenses(
                expenseCategory, isApproved, search != null ? search : "", pageable);
        
        List<ExpenseResponse> expenseResponses = expensesPage.getContent().stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
        
        PageResponse<ExpenseResponse> pageResponse = PageResponse.<ExpenseResponse>builder()
                .content(expenseResponses)
                .pageNumber(expensesPage.getNumber())
                .pageSize(expensesPage.getSize())
                .totalElements(expensesPage.getTotalElements())
                .totalPages(expensesPage.getTotalPages())
                .last(expensesPage.isLast())
                .first(expensesPage.isFirst())
                .build();
        
        log.info("Search completed: found {} expenses", expenseResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Search completed successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .expenseDate(expense.getExpenseDate())
                .vendor(expense.getVendor())
                .receiptNumber(expense.getReceiptNumber())
                .receiptUrl(expense.getReceiptUrl())
                .isApproved(expense.isApproved())
                .approvalDate(expense.getApprovalDate())
                .approvedBy(expense.getApprovedBy())
                .approvalRemarks(expense.getApprovalRemarks())
                .createdDate(expense.getCreatedDate())
                .updatedDate(expense.getUpdatedDate())
                .createdBy(expense.getCreatedBy())
                .updatedBy(expense.getUpdatedBy())
                .build();
    }
    
    private Long getCurrentUserId(String email) {
        try {
            return userRepository.findByEmail(email)
                    .map(User::getId)
                    .orElse(1L);
        } catch (Exception e) {
            log.warn("Could not get current user ID, using 1", e);
            return 1L;
        }
    }
}