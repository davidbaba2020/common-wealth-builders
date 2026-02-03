package com.common_wealth_builders.dto.response;

import com.common_wealth_builders.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    
    private Long id;
    private RoleType name;
    private String displayName;
    private String description;
    private boolean isActive;
    private boolean isSystemRole;
    private String code;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String createdBy;
    private String updatedBy;
    private Long version;
}