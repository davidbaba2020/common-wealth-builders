package com.common_wealth_builders.dto.request;

import com.common_wealth_builders.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {
    
    @NotNull(message = "Role name is required")
    private RoleType name;
    
    @NotBlank(message = "Display name is required")
    @Size(min = 3, max = 200, message = "Display name must be between 3 and 200 characters")
    private String displayName;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private Boolean isActive;
}