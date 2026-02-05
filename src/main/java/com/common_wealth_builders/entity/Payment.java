package com.common_wealth_builders.entity;

import com.common_wealth_builders.entity.base.BaseEntity;
import com.common_wealth_builders.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_user_id", columnList = "user_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_date", columnList = "paymentDate"),
        @Index(name = "idx_payment_verification", columnList = "isVerified")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Payment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime paymentDate;
    
    @Column(nullable = false, length = 100)
    private String paymentReference;
    
    @Column(length = 100)
    private String bankName;
    
    @Column(length = 50)
    private String accountNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;
    
    @Column(nullable = false)
    private boolean isVerified = false;
    
    @Column
    private LocalDateTime verificationDate;
    
    @Column(length = 100)
    private String verifiedBy;
    
    @Column(columnDefinition = "TEXT")
    private String verificationRemarks;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String proofOfPaymentUrl;
    
    @PrePersist
    protected void onCreate() {
        log.info("Creating new payment: userId={}, amount={}, reference={}", 
                 user != null ? user.getId() : null, amount, paymentReference);
        validatePayment();
    }
    
    @PreUpdate
    protected void onUpdate() {
        log.debug("Updating payment: id={}, status={}", getId(), status);
        validatePayment();
    }
    
    private void validatePayment() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid payment amount: {}", amount);
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        
        if (paymentReference == null || paymentReference.trim().isEmpty()) {
            log.error("Payment reference is required");
            throw new IllegalArgumentException("Payment reference is required");
        }
        
        log.trace("Payment validation passed");
    }
    
    public void verify(String verifiedBy, String remarks) {
        log.info("Verifying payment: id={}, verifiedBy={}", getId(), verifiedBy);
        
        if (this.isVerified) {
            log.warn("Payment already verified: id={}", getId());
            throw new IllegalStateException("Payment is already verified");
        }
        
        this.isVerified = true;
        this.status = PaymentStatus.VERIFIED;
        this.verificationDate = LocalDateTime.now();
        this.verifiedBy = verifiedBy;
        this.verificationRemarks = remarks;
        
        log.info("Payment verified successfully: id={}", getId());
    }
    
    public void reject(String rejectedBy, String remarks) {
        log.info("Rejecting payment: id={}, rejectedBy={}", getId(), rejectedBy);
        
        this.status = PaymentStatus.REJECTED;
        this.verificationDate = LocalDateTime.now();
        this.verifiedBy = rejectedBy;
        this.verificationRemarks = remarks;
        
        log.info("Payment rejected: id={}", getId());
    }
    
    public void cancel(String cancelledBy, String remarks) {
        log.info("Cancelling payment: id={}, cancelledBy={}", getId(), cancelledBy);
        
        if (this.isVerified) {
            log.warn("Cannot cancel verified payment: id={}", getId());
            throw new IllegalStateException("Cannot cancel a verified payment");
        }
        
        this.status = PaymentStatus.CANCELLED;
        this.verificationRemarks = remarks;
        
        log.info("Payment cancelled: id={}", getId());
    }
}