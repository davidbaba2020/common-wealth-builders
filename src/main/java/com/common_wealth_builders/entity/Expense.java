package com.common_wealth_builders.entity;

import com.common_wealth_builders.entity.base.BaseEntity;
import com.common_wealth_builders.enums.ExpenseCategory;
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
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_category", columnList = "category"),
        @Index(name = "idx_expense_date", columnList = "expenseDate"),
        @Index(name = "idx_expense_approved_by", columnList = "approvedBy")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Expense extends BaseEntity {
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExpenseCategory category;
    
    @Column(nullable = false)
    private LocalDateTime expenseDate;
    
    @Column(length = 100)
    private String vendor;
    
    @Column(length = 100)
    private String receiptNumber;
    
    @Column(columnDefinition = "TEXT")
    private String receiptUrl;
    
    @Column(nullable = false)
    private boolean isApproved = false;
    
    @Column
    private LocalDateTime approvalDate;
    
    @Column(length = 100)
    private String approvedBy;
    
    @Column(columnDefinition = "TEXT")
    private String approvalRemarks;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedByUser;
    
    @PrePersist
    protected void onCreate() {
        log.info("Creating new expense: title={}, amount={}, category={}", 
                 title, amount, category);
        validateExpense();
    }
    
    @PreUpdate
    protected void onUpdate() {
        log.debug("Updating expense: id={}, title={}", getId(), title);
        validateExpense();
    }
    
    private void validateExpense() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Invalid expense amount: {}", amount);
            throw new IllegalArgumentException("Expense amount must be greater than zero");
        }
        
        if (title == null || title.trim().isEmpty()) {
            log.error("Expense title is required");
            throw new IllegalArgumentException("Expense title is required");
        }
        
        log.trace("Expense validation passed");
    }
    
    public void approve(User approver, String remarks) {
        log.info("Approving expense: id={}, approvedBy={}", getId(), approver.getEmail());
        
        if (this.isApproved) {
            log.warn("Expense already approved: id={}", getId());
            throw new IllegalStateException("Expense is already approved");
        }
        
        this.isApproved = true;
        this.approvalDate = LocalDateTime.now();
        this.approvedBy = approver.getEmail();
        this.approvedByUser = approver;
        this.approvalRemarks = remarks;
        
        log.info("Expense approved successfully: id={}", getId());
    }
}