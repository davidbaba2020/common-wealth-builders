package com.common_wealth_builders.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class PaymentRequest {
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
    
    @NotNull(message = "Payment date is required")
    private LocalDateTime paymentDate;
    
    @NotBlank(message = "Payment reference is required")
    @Size(max = 100, message = "Payment reference cannot exceed 100 characters")
    private String paymentReference;
    
    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;
    
    @Size(max = 50, message = "Account number cannot exceed 50 characters")
    private String accountNumber;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private String proofOfPaymentUrl;
}