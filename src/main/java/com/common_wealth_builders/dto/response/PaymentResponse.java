package com.common_wealth_builders.dto.response;

import com.common_wealth_builders.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String paymentReference;
    private String bankName;
    private String accountNumber;
    private PaymentStatus status;
    private boolean isVerified;
    private LocalDateTime verificationDate;
    private String verifiedBy;
    private String verificationRemarks;
    private String description;
    private String proofOfPaymentUrl;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String createdBy;
    private String updatedBy;
}