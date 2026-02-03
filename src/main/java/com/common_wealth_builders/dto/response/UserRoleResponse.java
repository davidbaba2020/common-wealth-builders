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
public class UserRoleResponse {
    
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private Long roleId;
    private String roleName;
    private String roleDisplayName;
    private LocalDateTime assignedDate;
    private String assignedBy;
    private LocalDateTime revokedDate;
    private String revokedBy;
    private boolean isActive;
    private String remarks;
}