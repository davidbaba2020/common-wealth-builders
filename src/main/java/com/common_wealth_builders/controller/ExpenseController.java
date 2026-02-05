package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.ApproveExpenseRequest;
import com.common_wealth_builders.dto.request.ExpenseRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> createExpense(
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication) {
        
        log.info("Request received: POST /expenses - title={}, amount={}, category={}", 
                request.getTitle(), request.getAmount(), request.getCategory());
        
        GenericResponse response = expenseService.createExpense(request, authentication.getName());
        
        log.info("Response sent: POST /expenses - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getAllExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder) {
        
        log.info("Request received: GET /expenses - page={}, size={}, sortBy={}, sortOrder={}", 
                page, size, sortBy, sortOrder);
        
        Sort.Direction direction = sortOrder.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        GenericResponse response = expenseService.getAllExpenses(pageable);
        
        log.info("Response sent: GET /expenses - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getExpenseById(@PathVariable Long id) {
        log.info("Request received: GET /expenses/{} - id={}", id, id);
        
        GenericResponse response = expenseService.getExpenseById(id);
        
        log.info("Response sent: GET /expenses/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication) {
        
        log.info("Request received: PUT /expenses/{} - id={}, title={}", 
                id, id, request.getTitle());
        
        GenericResponse response = expenseService.updateExpense(id, request, authentication.getName());
        
        log.info("Response sent: PUT /expenses/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> deleteExpense(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Request received: DELETE /expenses/{} - id={}", id, id);
        
        GenericResponse response = expenseService.deleteExpense(id, authentication.getName());
        
        log.info("Response sent: DELETE /expenses/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> approveExpense(
            @PathVariable Long id,
            @Valid @RequestBody ApproveExpenseRequest request,
            Authentication authentication) {
        
        log.info("Request received: POST /expenses/{}/approve - id={}, approvedBy={}", 
                id, id, authentication.getName());
        
        GenericResponse response = expenseService.approveExpense(id, request, authentication.getName());
        
        log.info("Response sent: POST /expenses/{}/approve - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getPendingExpenses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /expenses/pending - page={}, size={}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"));
        GenericResponse response = expenseService.getPendingExpenses(pageable);
        
        log.info("Response sent: GET /expenses/pending - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> searchExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /expenses/search - category={}, isApproved={}, search={}, page={}, size={}", 
                category, isApproved, search, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"));
        GenericResponse response = expenseService.searchExpenses(category, isApproved, search, pageable);
        
        log.info("Response sent: GET /expenses/search - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}