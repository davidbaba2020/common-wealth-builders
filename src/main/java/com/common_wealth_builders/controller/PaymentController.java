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
@Tag(name = "Payment Management", description = "Payment operations - FIN_ADMIN handles verification")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @Operation(
            summary = "Create new payment",
            description = "Users can create payment records for their contributions. All authenticated users can make payments."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Payment created successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid payment data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericResponse> createPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment details",
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
            summary = "Get all payments (FIN ADMIN)",
            description = "Retrieves all payments. Only FIN_ADMIN and SUPER_ADMIN can view all payments."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires FIN_ADMIN role")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
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
            description = "Users can view their own payments, admins can view any payment."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment found",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "403", description = "Cannot view other user's payment")
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
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
            description = "Get payments for a specific user. Users can only see their own, admins can see any user's payments."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User payments retrieved successfully"
            ),
            @ApiResponse(responseCode = "403", description = "Cannot view other user's payments")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
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
            summary = "Get pending payments (FIN ADMIN)",
            description = "Retrieves payments pending verification. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending payments retrieved"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires FIN_ADMIN role")
    })
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
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
            summary = "Verify payment (FIN ADMIN ONLY)",
            description = "Verifies a payment after review. Only FIN_ADMIN and SUPER_ADMIN can verify payments."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment verified successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "400", description = "Payment already verified"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires FIN_ADMIN role")
    })
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> verifyPayment(
            @Parameter(description = "Payment ID to verify", example = "1", required = true)
            @PathVariable Long id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Verification details",
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
            summary = "Reject payment (FIN ADMIN ONLY)",
            description = "Rejects a payment. Only FIN_ADMIN and SUPER_ADMIN can reject payments."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment rejected successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires FIN_ADMIN role")
    })
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> rejectPayment(
            @Parameter(description = "Payment ID to reject", example = "1", required = true)
            @PathVariable Long id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Rejection details",
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
            description = "Users can cancel their own pending payments. Admins can cancel any payment."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment cancelled successfully"),
            @ApiResponse(responseCode = "403", description = "Cannot cancel verified payments or other user's payments")
    })
    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericResponse> cancelPayment(
            @Parameter(description = "Payment ID to cancel", example = "1", required = true)
            @PathVariable Long id,
            
            @Parameter(description = "Optional cancellation remarks")
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
            summary = "Search payments (FIN ADMIN)",
            description = "Search payments with filters. Only FIN_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires FIN_ADMIN role")
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> searchPayments(
            @Parameter(description = "User ID filter")
            @RequestParam(required = false) Long userId,
            
            @Parameter(description = "Payment status filter")
            @RequestParam(required = false) String status,
            
            @Parameter(description = "Verification status filter")
            @RequestParam(required = false) Boolean isVerified,
            
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /payments/search - userId={}, status={}, isVerified={}", 
                userId, status, isVerified);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        GenericResponse response = paymentService.searchPayments(userId, status, isVerified, pageable);
        
        log.info("Response sent: GET /payments/search - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}