package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.ApproveExpenseRequest;
import com.common_wealth_builders.dto.request.ExpenseRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.ExpenseService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/expenses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expense Management", description = "Expense operations - FIN_ADMIN handles expenses and approvals")
@SecurityRequirement(name = "Bearer Authentication")
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    @Operation(
            summary = "Create new expense (FIN ADMIN ONLY)",
            description = "Creates expense record. Only FIN_ADMIN and SUPER_ADMIN can create expenses."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Expense created successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid expense data"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires FIN_ADMIN role")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> createExpense(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Expense details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExpenseRequest.class))
            )
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication) {
        
        log.info("Request received: POST /expenses - title={}, amount={}", 
                request.getTitle(), request.getAmount());
        
        GenericResponse response = expenseService.createExpense(request, authentication.getName());
        
        log.info("Response sent: POST /expenses - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get all expenses (FIN ADMIN)",
            description = "Retrieves all expenses. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires FIN_ADMIN role")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getAllExpenses(
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sort field", example = "expenseDate")
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            
            @Parameter(description = "Sort direction", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortOrder) {
        
        log.info("Request received: GET /expenses - page={}, size={}", page, size);
        
        Sort.Direction direction = sortOrder.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        GenericResponse response = expenseService.getAllExpenses(pageable);
        
        log.info("Response sent: GET /expenses - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get expense by ID (FIN ADMIN)",
            description = "Retrieves expense details. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense found"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getExpenseById(
            @Parameter(description = "Expense ID", example = "1", required = true)
            @PathVariable Long id) {
        
        log.info("Request received: GET /expenses/{}", id);
        
        GenericResponse response = expenseService.getExpenseById(id);
        
        log.info("Response sent: GET /expenses/{} - status={}", id, response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Update expense (FIN ADMIN)",
            description = "Updates an existing expense. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense updated"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> updateExpense(
            @Parameter(description = "Expense ID", example = "1", required = true)
            @PathVariable Long id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated expense details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExpenseRequest.class))
            )
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication) {
        
        log.info("Request received: PUT /expenses/{}", id);
        
        GenericResponse response = expenseService.updateExpense(id, request, authentication.getName());
        
        log.info("Response sent: PUT /expenses/{} - status={}", id, response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Delete expense (SUPER ADMIN ONLY)",
            description = "Permanently deletes an expense. Only SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN role")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> deleteExpense(
            @Parameter(description = "Expense ID", example = "1", required = true)
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Request received: DELETE /expenses/{}", id);
        
        GenericResponse response = expenseService.deleteExpense(id, authentication.getName());
        
        log.info("Response sent: DELETE /expenses/{} - status={}", id, response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Approve expense (FIN ADMIN)",
            description = "Approves an expense. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense approved"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires FIN_ADMIN role")
    })
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> approveExpense(
            @Parameter(description = "Expense ID", example = "1", required = true)
            @PathVariable Long id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Approval details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ApproveExpenseRequest.class))
            )
            @Valid @RequestBody ApproveExpenseRequest request,
            Authentication authentication) {
        
        log.info("Request received: POST /expenses/{}/approve", id);
        
        GenericResponse response = expenseService.approveExpense(id, request, authentication.getName());
        
        log.info("Response sent: POST /expenses/{}/approve - status={}", id, response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get pending expenses (FIN ADMIN)",
            description = "Retrieves expenses pending approval. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getPendingExpenses(
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /expenses/pending");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"));
        GenericResponse response = expenseService.getPendingExpenses(pageable);
        
        log.info("Response sent: GET /expenses/pending - status={}", response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Search expenses (FIN ADMIN)",
            description = "Search expenses with filters. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> searchExpenses(
            @Parameter(description = "Expense category")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Approval status")
            @RequestParam(required = false) Boolean isApproved,
            
            @Parameter(description = "Search term")
            @RequestParam(required = false) String search,
            
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /expenses/search");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"));
        GenericResponse response = expenseService.searchExpenses(category, isApproved, search, pageable);
        
        log.info("Response sent: GET /expenses/search - status={}", response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}