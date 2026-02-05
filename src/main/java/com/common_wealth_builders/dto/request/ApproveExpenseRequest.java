package com.common_wealth_builders.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveExpenseRequest {
    
    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters")
    private String remarks;
}