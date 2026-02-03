package com.common_wealth_builders.entity;

import com.common_wealth_builders.entity.base.BaseEntity;
import com.common_wealth_builders.enums.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name"),
        @Index(name = "idx_role_is_active", columnList = "isActive"),
        @Index(name = "idx_role_deleted", columnList = "isDeleted")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Role extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private RoleType name;
    
    @Column(nullable = false, length = 200)
    private String displayName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    @Column(nullable = false)
    private boolean isSystemRole = false;
    
    @Column(length = 100)
    private String code;
    
    @PrePersist
    protected void onCreate() {
        log.debug("Creating new role: name={}, displayName={}", name, displayName);
        validateRole();
        generateCodeIfNull();
    }
    
    @PreUpdate
    protected void onUpdate() {
        log.debug("Updating role: id={}, name={}", getId(), name);
        validateRole();
    }
    
    private void validateRole() {
        if (name == null) {
            log.error("Role name cannot be null");
            throw new IllegalArgumentException("Role name is required");
        }
        
        if (displayName == null || displayName.trim().isEmpty()) {
            log.error("Role displayName cannot be empty for role: {}", name);
            throw new IllegalArgumentException("Role display name is required");
        }
        
        log.trace("Role validation passed for: {}", name);
    }
    
    private void generateCodeIfNull() {
        if (code == null && name != null) {
            this.code = "ROLE_" + name.name();
            log.debug("Generated role code: {}", code);
        }
    }
    
    public void activate() {
        log.info("Activating role: id={}, name={}", getId(), name);
        this.isActive = true;
    }
    
    public void deactivate() {
        if (isSystemRole) {
            log.warn("Cannot deactivate system role: id={}, name={}", getId(), name);
            throw new IllegalStateException("Cannot deactivate system role");
        }
        log.info("Deactivating role: id={}, name={}", getId(), name);
        this.isActive = false;
    }
    
    @Override
    public void softDelete(String deletedBy) {
        if (isSystemRole) {
            log.warn("Attempt to delete system role blocked: id={}, name={}", getId(), name);
            throw new IllegalStateException("Cannot delete system role");
        }
        log.info("Soft deleting role: id={}, name={}, deletedBy={}", getId(), name, deletedBy);
        super.softDelete(deletedBy);
    }
    
    public boolean isSystemRole() {
        return isSystemRole;
    }
}