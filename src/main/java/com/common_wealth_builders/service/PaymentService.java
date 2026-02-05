package com.common_wealth_builders.service;

import com.common_wealth_builders.dto.request.PaymentRequest;
import com.common_wealth_builders.dto.request.VerifyPaymentRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    GenericResponse createPayment(PaymentRequest request, String userEmail);
    GenericResponse getAllPayments(Pageable pageable);
    GenericResponse getPaymentById(Long id);
    GenericResponse getUserPayments(Long userId, Pageable pageable);
    GenericResponse getPendingPayments(Pageable pageable);
    GenericResponse verifyPayment(Long id, VerifyPaymentRequest request, String verifiedBy);
    GenericResponse rejectPayment(Long id, VerifyPaymentRequest request, String rejectedBy);
    GenericResponse cancelPayment(Long id, String remarks, String userEmail);
    GenericResponse searchPayments(Long userId, String status, Boolean isVerified, Pageable pageable);
}