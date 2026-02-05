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
@Tag(name = "Expense Management", description = "Endpoints for managing organizational expenses and approvals")
@SecurityRequirement(name = "Bearer Authentication")
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    @Operation(
            summary = "Create new expense",
            description = "Creates a new expense record for the organization. Expenses require administrative approval before being finalized."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Expense created successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid expense data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> createExpense(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Expense details including title, amount, category, and description",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExpenseRequest.class))
            )
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication) {
        
        log.info("Request received: POST /expenses - title={}, amount={}, category={}", 
                request.getTitle(), request.getAmount(), request.getCategory());
        
        GenericResponse response = expenseService.createExpense(request, authentication.getName());
        
        log.info("Response sent: POST /expenses - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get all expenses",
            description = "Retrieves a paginated list of all expenses with sorting options. Only accessible by financial administrators."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Expenses retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getAllExpenses(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Field to sort by", example = "expenseDate")
            @RequestParam(defaultValue = "expenseDate") String sortBy,
            
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
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
    
    @Operation(
            summary = "Get expense by ID",
            description = "Retrieves detailed information about a specific expense"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense found and returned successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getExpenseById(
            @Parameter(description = "Expense ID", example = "1", required = true)
            @PathVariable Long id) {
        
        log.info("Request received: GET /expenses/{} - id={}", id, id);
        
        GenericResponse response = expenseService.getExpenseById(id);
        
        log.info("Response sent: GET /expenses/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Update expense",
            description = "Updates an existing expense. Only pending expenses can be updated."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense updated successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "400", description = "Invalid data or expense cannot be updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> updateExpense(
            @Parameter(description = "Expense ID to update", example = "1", required = true)
            @PathVariable Long id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated expense details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExpenseRequest.class))
            )
            @Valid @RequestBody ExpenseRequest request,
            Authentication authentication) {
        
        log.info("Request received: PUT /expenses/{} - id={}, title={}", 
                id, id, request.getTitle());
        
        GenericResponse response = expenseService.updateExpense(id, request, authentication.getName());
        
        log.info("Response sent: PUT /expenses/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Delete expense",
            description = "Permanently deletes an expense record. Only accessible by SUPER_ADMIN. This action cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense deleted successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "400", description = "Expense cannot be deleted (e.g., already approved)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN role")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> deleteExpense(
            @Parameter(description = "Expense ID to delete", example = "1", required = true)
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Request received: DELETE /expenses/{} - id={}", id, id);
        
        GenericResponse response = expenseService.deleteExpense(id, authentication.getName());
        
        log.info("Response sent: DELETE /expenses/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Approve expense",
            description = "Approves an expense after review. Changes expense status to APPROVED and makes it final."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Expense approved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "400", description = "Expense already approved or in invalid state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> approveExpense(
            @Parameter(description = "Expense ID to approve", example = "1", required = true)
            @PathVariable Long id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Approval details including remarks",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ApproveExpenseRequest.class))
            )
            @Valid @RequestBody ApproveExpenseRequest request,
            Authentication authentication) {
        
        log.info("Request received: POST /expenses/{}/approve - id={}, approvedBy={}", 
                id, id, authentication.getName());
        
        GenericResponse response = expenseService.approveExpense(id, request, authentication.getName());
        
        log.info("Response sent: POST /expenses/{}/approve - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get pending expenses",
            description = "Retrieves all expenses that are pending approval"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pending expenses retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getPendingExpenses(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /expenses/pending - page={}, size={}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "expenseDate"));
        GenericResponse response = expenseService.getPendingExpenses(pageable);
        
        log.info("Response sent: GET /expenses/pending - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Search expenses",
            description = "Searches expenses with filters for category, approval status, and text search"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> searchExpenses(
            @Parameter(description = "Expense category filter", example = "UTILITIES")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Approval status filter", example = "true")
            @RequestParam(required = false) Boolean isApproved,
            
            @Parameter(description = "Text search in title or description", example = "office")
            @RequestParam(required = false) String search,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "10")
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