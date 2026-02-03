package com.common_wealth_builders.entity;

import com.common_wealth_builders.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_roles", 
    indexes = {
        @Index(name = "idx_user_role_user_id", columnList = "user_id"),
        @Index(name = "idx_user_role_role_id", columnList = "role_id"),
        @Index(name = "idx_user_role_assigned_date", columnList = "assignedDate"),
        @Index(name = "idx_user_role_active", columnList = "isActive")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_id"})
    }
)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class UserRole extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @Column(nullable = false)
    private LocalDateTime assignedDate;
    
    @Column(length = 100)
    private String assignedBy;
    
    @Column
    private LocalDateTime revokedDate;
    
    @Column(length = 100)
    private String revokedBy;
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    @Column(length = 500)
    private String remarks;
    
    @PrePersist
    protected void onCreate() {
        if (assignedDate == null) {
            assignedDate = LocalDateTime.now();
        }
        log.info("Assigning role to user: userId={}, roleId={}, assignedBy={}", 
                 user != null ? user.getId() : null, 
                 role != null ? role.getId() : null, 
                 assignedBy);
    }
    
    public void revoke(String revokedBy) {
        log.info("Revoking role from user: userId={}, roleId={}, revokedBy={}", 
                 user.getId(), role.getId(), revokedBy);
        this.isActive = false;
        this.revokedDate = LocalDateTime.now();
        this.revokedBy = revokedBy;
    }
    
    public void reactivate(String reactivatedBy) {
        log.info("Reactivating role for user: userId={}, roleId={}, reactivatedBy={}", 
                 user.getId(), role.getId(), reactivatedBy);
        this.isActive = true;
        this.revokedDate = null;
        this.revokedBy = null;
        this.assignedBy = reactivatedBy;
        this.assignedDate = LocalDateTime.now();
    }
}