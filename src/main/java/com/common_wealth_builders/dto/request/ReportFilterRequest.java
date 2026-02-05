package com.common_wealth_builders.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFilterRequest {
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private Long userId;
    
    private String reportType;
}