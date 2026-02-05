package com.common_wealth_builders.repository;

import com.common_wealth_builders.entity.Payment;
import com.common_wealth_builders.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Page<Payment> findByUserId(Long userId, Pageable pageable);
    
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
    
    Page<Payment> findByIsVerified(boolean isVerified, Pageable pageable);
    
    List<Payment> findByUserIdAndIsVerifiedTrue(Long userId);
    
    Optional<Payment> findByPaymentReference(String paymentReference);
    
    @Query("SELECT p FROM Payment p WHERE " +
           "p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByPaymentDateBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE " +
           "p.isVerified = true AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumVerifiedPaymentsBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE " +
           "p.user.id = :userId AND p.isVerified = true")
    BigDecimal sumVerifiedPaymentsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Payment p WHERE " +
           "(:userId IS NULL OR p.user.id = :userId) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:isVerified IS NULL OR p.isVerified = :isVerified)")
    Page<Payment> searchPayments(
        @Param("userId") Long userId,
        @Param("status") PaymentStatus status,
        @Param("isVerified") Boolean isVerified,
        Pageable pageable
    );
}