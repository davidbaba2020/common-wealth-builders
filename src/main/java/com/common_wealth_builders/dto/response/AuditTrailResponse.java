package com.common_wealth_builders.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrailResponse {
    
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private String action;
    private String module;
    private String description;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdDate;
    private String createdBy;
}