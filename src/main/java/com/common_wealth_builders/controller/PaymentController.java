package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.PaymentRequest;
import com.common_wealth_builders.dto.request.VerifyPaymentRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN', 'ROLE_USER')")
    public ResponseEntity<GenericResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        
        log.info("Request received: POST /payments - user={}, amount={}", 
                authentication.getName(), request.getAmount());
        
        GenericResponse response = paymentService.createPayment(request, authentication.getName());
        
        log.info("Response sent: POST /payments - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
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
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN', 'ROLE_USER')")
    public ResponseEntity<GenericResponse> getPaymentById(@PathVariable Long id) {
        log.info("Request received: GET /payments/{} - id={}", id, id);
        
        GenericResponse response = paymentService.getPaymentById(id);
        
        log.info("Response sent: GET /payments/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN', 'ROLE_USER')")
    public ResponseEntity<GenericResponse> getUserPayments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /payments/user/{} - userId={}, page={}, size={}", 
                userId, userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        GenericResponse response = paymentService.getUserPayments(userId, pageable);
        
        log.info("Response sent: GET /payments/user/{} - status={}, success={}", 
                userId, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> getPendingPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /payments/pending - page={}, size={}", page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate"));
        GenericResponse response = paymentService.getPendingPayments(pageable);
        
        log.info("Response sent: GET /payments/pending - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> verifyPayment(
            @PathVariable Long id,
            @Valid @RequestBody VerifyPaymentRequest request,
            Authentication authentication) {
        
        log.info("Request received: PUT /payments/{}/verify - id={}, verifiedBy={}", 
                id, id, authentication.getName());
        
        GenericResponse response = paymentService.verifyPayment(id, request, authentication.getName());
        
        log.info("Response sent: PUT /payments/{}/verify - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> rejectPayment(
            @PathVariable Long id,
            @Valid @RequestBody VerifyPaymentRequest request,
            Authentication authentication) {
        
        log.info("Request received: PUT /payments/{}/reject - id={}, rejectedBy={}", 
                id, id, authentication.getName());
        
        GenericResponse response = paymentService.rejectPayment(id, request, authentication.getName());
        
        log.info("Response sent: PUT /payments/{}/reject - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN', 'ROLE_USER')")
    public ResponseEntity<GenericResponse> cancelPayment(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks,
            Authentication authentication) {
        
        log.info("Request received: PUT /payments/{}/cancel - id={}, cancelledBy={}", 
                id, id, authentication.getName());
        
        GenericResponse response = paymentService.cancelPayment(id, remarks, authentication.getName());
        
        log.info("Response sent: PUT /payments/{}/cancel - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN', 'ROLE_FIN_ADMIN')")
    public ResponseEntity<GenericResponse> searchPayments(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(defaultValue = "0") int page,
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