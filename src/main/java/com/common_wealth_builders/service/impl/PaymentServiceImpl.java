package com.common_wealth_builders.service.impl;

import com.common_wealth_builders.dto.request.PaymentRequest;
import com.common_wealth_builders.dto.request.VerifyPaymentRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.dto.response.PageResponse;
import com.common_wealth_builders.dto.response.PaymentResponse;
import com.common_wealth_builders.entity.Payment;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.enums.PaymentStatus;
import com.common_wealth_builders.exception.PaymentAlreadyVerifiedException;
import com.common_wealth_builders.exception.ResourceNotFoundException;
import com.common_wealth_builders.repository.PaymentRepository;
import com.common_wealth_builders.repository.UserRepository;
import com.common_wealth_builders.service.AuditService;
import com.common_wealth_builders.service.PaymentService;
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
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    
    @Override
    @Transactional
    public GenericResponse createPayment(PaymentRequest request, String userEmail) {
        log.info("Creating payment for user: {}, amount: {}", userEmail, request.getAmount());
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Payment payment = Payment.builder()
                .user(user)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate())
                .paymentReference(request.getPaymentReference())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .status(PaymentStatus.PENDING)
                .description(request.getDescription())
                .proofOfPaymentUrl(request.getProofOfPaymentUrl())
                .isVerified(false)
                .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        
        auditService.logAction(
                user.getId(),
                "PAYMENT_CREATED",
                "PAYMENTS",
                "Payment created with reference: " + savedPayment.getPaymentReference()
        );
        
        log.info("Payment created successfully: id={}, reference={}", 
                savedPayment.getId(), savedPayment.getPaymentReference());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Payment created successfully. Awaiting verification.")
                .data(mapToPaymentResponse(savedPayment))
                .httpStatus(HttpStatus.CREATED)
                .build();
    }
    
    @Override
    public GenericResponse getAllPayments(Pageable pageable) {
        log.info("Fetching all payments with pagination");
        
        Page<Payment> paymentsPage = paymentRepository.findAll(pageable);
        
        List<PaymentResponse> paymentResponses = paymentsPage.getContent().stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
        
        PageResponse<PaymentResponse> pageResponse = PageResponse.<PaymentResponse>builder()
                .content(paymentResponses)
                .pageNumber(paymentsPage.getNumber())
                .pageSize(paymentsPage.getSize())
                .totalElements(paymentsPage.getTotalElements())
                .totalPages(paymentsPage.getTotalPages())
                .last(paymentsPage.isLast())
                .first(paymentsPage.isFirst())
                .build();
        
        log.info("Successfully fetched {} payments", paymentResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Payments retrieved successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getPaymentById(Long id) {
        log.info("Fetching payment by ID: {}", id);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        
        log.info("Payment retrieved successfully: id={}", id);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Payment retrieved successfully")
                .data(mapToPaymentResponse(payment))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getUserPayments(Long userId, Pageable pageable) {
        log.info("Fetching payments for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Page<Payment> paymentsPage = paymentRepository.findByUserId(userId, pageable);
        
        List<PaymentResponse> paymentResponses = paymentsPage.getContent().stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
        
        PageResponse<PaymentResponse> pageResponse = PageResponse.<PaymentResponse>builder()
                .content(paymentResponses)
                .pageNumber(paymentsPage.getNumber())
                .pageSize(paymentsPage.getSize())
                .totalElements(paymentsPage.getTotalElements())
                .totalPages(paymentsPage.getTotalPages())
                .last(paymentsPage.isLast())
                .first(paymentsPage.isFirst())
                .build();
        
        log.info("Found {} payments for user: {}", paymentResponses.size(), user.getEmail());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("User payments retrieved successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getPendingPayments(Pageable pageable) {
        log.info("Fetching pending payments");
        
        Page<Payment> paymentsPage = paymentRepository.findByStatus(PaymentStatus.PENDING, pageable);
        
        List<PaymentResponse> paymentResponses = paymentsPage.getContent().stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
        
        PageResponse<PaymentResponse> pageResponse = PageResponse.<PaymentResponse>builder()
                .content(paymentResponses)
                .pageNumber(paymentsPage.getNumber())
                .pageSize(paymentsPage.getSize())
                .totalElements(paymentsPage.getTotalElements())
                .totalPages(paymentsPage.getTotalPages())
                .last(paymentsPage.isLast())
                .first(paymentsPage.isFirst())
                .build();
        
        log.info("Found {} pending payments", paymentResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Pending payments retrieved successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional
    public GenericResponse verifyPayment(Long id, VerifyPaymentRequest request, String verifiedBy) {
        log.info("Verifying payment: id={}, verifiedBy={}", id, verifiedBy);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        
        if (payment.isVerified()) {
            throw new PaymentAlreadyVerifiedException("Payment is already verified");
        }
        
        payment.verify(verifiedBy, request.getRemarks());
        Payment verifiedPayment = paymentRepository.save(payment);
        
        auditService.logAction(
                payment.getUser().getId(),
                "PAYMENT_VERIFIED",
                "PAYMENTS",
                "Payment verified: " + payment.getPaymentReference() + " by " + verifiedBy
        );
        
        log.info("Payment verified successfully: id={}", id);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Payment verified successfully")
                .data(mapToPaymentResponse(verifiedPayment))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional
    public GenericResponse rejectPayment(Long id, VerifyPaymentRequest request, String rejectedBy) {
        log.info("Rejecting payment: id={}, rejectedBy={}", id, rejectedBy);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        
        payment.reject(rejectedBy, request.getRemarks());
        Payment rejectedPayment = paymentRepository.save(payment);
        
        auditService.logAction(
                payment.getUser().getId(),
                "PAYMENT_REJECTED",
                "PAYMENTS",
                "Payment rejected: " + payment.getPaymentReference() + " by " + rejectedBy
        );
        
        log.info("Payment rejected: id={}", id);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Payment rejected")
                .data(mapToPaymentResponse(rejectedPayment))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional
    public GenericResponse cancelPayment(Long id, String remarks, String userEmail) {
        log.info("Cancelling payment: id={}, cancelledBy={}", id, userEmail);
        
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        
        payment.cancel(userEmail, remarks);
        Payment cancelledPayment = paymentRepository.save(payment);
        
        auditService.logAction(
                payment.getUser().getId(),
                "PAYMENT_CANCELLED",
                "PAYMENTS",
                "Payment cancelled: " + payment.getPaymentReference()
        );
        
        log.info("Payment cancelled: id={}", id);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Payment cancelled successfully")
                .data(mapToPaymentResponse(cancelledPayment))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse searchPayments(Long userId, String status, Boolean isVerified, Pageable pageable) {
        log.info("Searching payments: userId={}, status={}, isVerified={}", userId, status, isVerified);
        
        PaymentStatus paymentStatus = status != null ? PaymentStatus.valueOf(status.toUpperCase()) : null;
        
        Page<Payment> paymentsPage = paymentRepository.searchPayments(userId, paymentStatus, isVerified, pageable);
        
        List<PaymentResponse> paymentResponses = paymentsPage.getContent().stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
        
        PageResponse<PaymentResponse> pageResponse = PageResponse.<PaymentResponse>builder()
                .content(paymentResponses)
                .pageNumber(paymentsPage.getNumber())
                .pageSize(paymentsPage.getSize())
                .totalElements(paymentsPage.getTotalElements())
                .totalPages(paymentsPage.getTotalPages())
                .last(paymentsPage.isLast())
                .first(paymentsPage.isFirst())
                .build();
        
        log.info("Search completed: found {} payments", paymentResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Search completed successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUser().getId())
                .userEmail(payment.getUser().getEmail())
                .userFullName(payment.getUser().getFirstname() + " " + payment.getUser().getLastname())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentReference(payment.getPaymentReference())
                .bankName(payment.getBankName())
                .accountNumber(payment.getAccountNumber())
                .status(payment.getStatus())
                .isVerified(payment.isVerified())
                .verificationDate(payment.getVerificationDate())
                .verifiedBy(payment.getVerifiedBy())
                .verificationRemarks(payment.getVerificationRemarks())
                .description(payment.getDescription())
                .proofOfPaymentUrl(payment.getProofOfPaymentUrl())
                .createdDate(payment.getCreatedDate())
                .updatedDate(payment.getUpdatedDate())
                .createdBy(payment.getCreatedBy())
                .updatedBy(payment.getUpdatedBy())
                .build();
    }
}