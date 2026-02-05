package com.common_wealth_builders.dto.response;

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
public class UserContributionResponse {
    
    private Long userId;
    private String userEmail;
    private String userFullName;
    private BigDecimal totalContributions;
    private BigDecimal verifiedContributions;
    private BigDecimal pendingContributions;
    private Integer paymentCount;
    private LocalDateTime lastPaymentDate;
    private LocalDateTime firstPaymentDate;
}