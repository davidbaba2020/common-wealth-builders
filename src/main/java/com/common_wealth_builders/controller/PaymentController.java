package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.PaymentRequest;
import com.common_wealth_builders.dto.request.VerifyPaymentRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.PaymentService;
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
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Management", description = "Endpoints for managing payments, contributions, and payment verification")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @Operation(
            summary = "Create new payment",
            description = "Creates a new payment record for a user contribution. The payment will be in PENDING status until verified by an administrator."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Payment created successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid payment data or duplicate payment"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN', 'ROLE_USER')")
    public ResponseEntity<GenericResponse> createPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment details including amount, payment method, and reference",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentRequest.class))
            )
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        
        log.info("Request received: POST /payments - user={}, amount={}", 
                authentication.getName(), request.getAmount());
        
        GenericResponse response = paymentService.createPayment(request, authentication.getName());
        
        log.info("Response sent: POST /payments - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get all payments",
            description = "Retrieves a paginated list of all payments in the system with sorting options. Only accessible by administrators."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getAllPayments(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Field to sort by", example = "paymentDate")
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortOrder) {
        
        log.info("Request received: GET /payments - page={}, size={}, sortBy={}, sortOrder={}", 
                page, size, sortBy, sortOrder);
        
        Sort.Direction direction = sortOrder.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        GenericResponse response = paymentService.getAllPayments(pageable);
        
        log.info("Response sent: GET /payments - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get payment by ID",
            description = "Retrieves detailed information about a specific payment"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment found and returned successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN', 'ROLE_USER')")
    public ResponseEntity<GenericResponse> getPaymentById(
            @Parameter(description = "Payment ID", example = "1", required = true)
            @PathVariable Long id) {
        
        log.info("Request received: GET /payments/{} - id={}", id, id);
        
        GenericResponse response = paymentService.getPaymentById(id);
        
        log.info("Response sent: GET /payments/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get user payments",
            description = "Retrieves all payments made by a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User payments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN', 'ROLE_USER')")
    public ResponseEntity<GenericResponse> getUserPayments(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /payments/user/{} - userId={}, page={}, size={}", 
                userId, userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        GenericResponse response = paymentService.getUserPayments(userId, pageable);
        
        log.info("Response sent: GET /payments/user/{} - status={}, success={}", 
                userId, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get pending payments",
            description = "Retrieves all payments that are pending verification by administrators"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pending payments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getPendingPayments(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /payments/pending - page={}, size={}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        GenericResponse response = paymentService.getPendingPayments(pageable);
        
        log.info("Response sent: GET /payments/pending - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Verify payment",
            description = "Verifies a payment after administrative review. Updates payment status to VERIFIED."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment verified successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "400", description = "Payment already verified or in invalid state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> verifyPayment(
            @Parameter(description = "Payment ID to verify", example = "1", required = true)
            @PathVariable Long id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Verification details including remarks",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VerifyPaymentRequest.class))
            )
            @Valid @RequestBody VerifyPaymentRequest request,
            Authentication authentication) {
        
        log.info("Request received: PUT /payments/{}/verify - id={}, verifiedBy={}", 
                id, id, authentication.getName());
        
        GenericResponse response = paymentService.verifyPayment(id, request, authentication.getName());
        
        log.info("Response sent: PUT /payments/{}/verify - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Reject payment",
            description = "Rejects a payment after administrative review with specified reason. Updates payment status to REJECTED."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment rejected successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "400", description = "Payment already processed or in invalid state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> rejectPayment(
            @Parameter(description = "Payment ID to reject", example = "1", required = true)
            @PathVariable Long id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Rejection details including reason",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VerifyPaymentRequest.class))
            )
            @Valid @RequestBody VerifyPaymentRequest request,
            Authentication authentication) {
        
        log.info("Request received: PUT /payments/{}/reject - id={}, rejectedBy={}", 
                id, id, authentication.getName());
        
        GenericResponse response = paymentService.rejectPayment(id, request, authentication.getName());
        
        log.info("Response sent: PUT /payments/{}/reject - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Cancel payment",
            description = "Cancels a payment. Users can cancel their own pending payments, administrators can cancel any payment."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment cancelled successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "400", description = "Payment cannot be cancelled in current state"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Cannot cancel another user's payment")
    })
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN', 'ROLE_USER')")
    public ResponseEntity<GenericResponse> cancelPayment(
            @Parameter(description = "Payment ID to cancel", example = "1", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "Optional cancellation remarks", example = "Accidental duplicate payment")
            @RequestParam(required = false) String remarks,
            Authentication authentication) {
        
        log.info("Request received: PUT /payments/{}/cancel - id={}, cancelledBy={}", 
                id, id, authentication.getName());
        
        GenericResponse response = paymentService.cancelPayment(id, remarks, authentication.getName());
        
        log.info("Response sent: PUT /payments/{}/cancel - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Search payments",
            description = "Searches payments with various filters including user, status, and verification state"
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
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> searchPayments(
            @Parameter(description = "User ID to filter by", example = "1")
            @RequestParam(required = false) Long userId,
            
            @Parameter(description = "Payment status (PENDING, VERIFIED, REJECTED, CANCELLED)", example = "PENDING")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Verification status filter", example = "true")
            @RequestParam(required = false) Boolean isVerified,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /payments/search - userId={}, status={}, isVerified={}, page={}, size={}", 
                userId, status, isVerified, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        GenericResponse response = paymentService.searchPayments(userId, status, isVerified, pageable);
        
        log.info("Response sent: GET /payments/search - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}