package com.common_wealth_builders.dto.response;

import com.common_wealth_builders.enums.ExpenseCategory;
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
public class ExpenseResponse {
    
    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private ExpenseCategory category;
    private LocalDateTime expenseDate;
    private String vendor;
    private String receiptNumber;
    private String receiptUrl;
    private boolean isApproved;
    private LocalDateTime approvalDate;
    private String approvedBy;
    private String approvalRemarks;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String createdBy;
    private String updatedBy;
}