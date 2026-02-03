package com.common_wealth_builders.entity;

import com.common_wealth_builders.entity.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_username", columnList = "userName"),
        @Index(name = "idx_user_phone", columnList = "phoneNumber"),
        @Index(name = "idx_user_created_date", columnList = "createdDate"),
        @Index(name = "idx_user_enabled", columnList = "isEnabled")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"userRoles"})
@Slf4j
public class User extends BaseEntity implements UserDetails {
    
    @Column(nullable = false, length = 100)
    private String firstname;
    
    @Column(nullable = false, length = 100)
    private String lastname;
    
    @Column(nullable = false, unique = true, length = 255)
    private String email;
    
    @Column(length = 50)
    private String passport;
    
    @Column(nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(length = 100)
    private String remitanceBankName;
    
    @Column(length = 20)
    private String remitanceAccNumber;
    
    @Column(nullable = false, unique = true, length = 50)
    private String userName;
    
    @Column(nullable = false, length = 255)
    private String password;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isEnabled = true;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isAccountNonExpired = true;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isAccountNonLocked = true;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isCredentialsNonExpired = true;
    
    @Column
    private LocalDateTime lastLoginDate;
    
    @Column(length = 45)
    private String lastLoginIp;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    
    @Column
    private LocalDateTime accountLockedUntil;
    
    @PrePersist
    protected void onCreate() {
        log.info("Creating new user: email={}, userName={}", email, userName);
        validateUser();
    }
    
    @PreUpdate
    protected void onUpdate() {
        log.debug("Updating user: id={}, email={}", getId(), email);
        validateUser();
    }
    
    private void validateUser() {
        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            log.error("Invalid email format: {}", email);
            throw new IllegalArgumentException("Invalid email format");
        }
        
        if (phoneNumber == null || !phoneNumber.matches("^\\+?[0-9]{10,15}$")) {
            log.error("Invalid phone number format: {}", phoneNumber);
            throw new IllegalArgumentException("Invalid phone number format");
        }
        
        log.trace("User validation passed for: {}", email);
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        if (userRoles != null) {
            authorities = userRoles.stream()
                    .filter(UserRole::isActive)
                    .filter(ur -> ur.getRole() != null && ur.getRole().isActive())
                    .map(ur -> new SimpleGrantedAuthority(ur.getRole().getCode()))
                    .collect(Collectors.toList());
        }
        
        log.trace("User {} has {} authorities", email, authorities.size());
        return authorities;
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return isAccountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        if (accountLockedUntil != null && LocalDateTime.now().isBefore(accountLockedUntil)) {
            log.debug("Account is locked until: {} for user: {}", accountLockedUntil, email);
            return false;
        }
        return isAccountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return isCredentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
    
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        log.warn("Failed login attempt #{} for user: {}", failedLoginAttempts, email);
        
        if (this.failedLoginAttempts >= 5) {
            this.isAccountNonLocked = false;
            this.accountLockedUntil = LocalDateTime.now().plusHours(1);
            log.error("Account locked for user: {} until: {}", email, accountLockedUntil);
        }
    }
    
    public void resetFailedLoginAttempts() {
        log.info("Resetting failed login attempts for user: {}", email);
        this.failedLoginAttempts = 0;
        this.isAccountNonLocked = true;
        this.accountLockedUntil = null;
    }
    
    public void updateLastLogin(String ipAddress) {
        log.info("Updating last login for user: {} from IP: {}", email, ipAddress);
        this.lastLoginDate = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        resetFailedLoginAttempts();
    }
    
    public void assignRole(Role role, String assignedBy) {
        log.info("Assigning role {} to user: {}, by: {}", role.getName(), email, assignedBy);
        
        boolean exists = userRoles.stream()
                .anyMatch(ur -> ur.getRole().getId().equals(role.getId()) && ur.isActive());
        
        if (exists) {
            log.warn("Role {} already assigned to user: {}", role.getName(), email);
            throw new IllegalStateException("Role already assigned to user");
        }
        
        UserRole userRole = UserRole.builder()
                .user(this)
                .role(role)
                .assignedDate(LocalDateTime.now())
                .assignedBy(assignedBy)
                .isActive(true)
                .build();
        
        userRoles.add(userRole);
        log.info("Role {} successfully assigned to user: {}", role.getName(), email);
    }
    
    public void revokeRole(Role role, String revokedBy) {
        log.info("Revoking role {} from user: {}, by: {}", role.getName(), email, revokedBy);
        
        userRoles.stream()
                .filter(ur -> ur.getRole().getId().equals(role.getId()) && ur.isActive())
                .findFirst()
                .ifPresentOrElse(
                        ur -> {
                            ur.revoke(revokedBy);
                            log.info("Role {} successfully revoked from user: {}", role.getName(), email);
                        },
                        () -> log.warn("Active role {} not found for user: {}", role.getName(), email)
                );
    }
    
    public boolean hasRole(String roleName) {
        boolean hasRole = userRoles.stream()
                .filter(UserRole::isActive)
                .anyMatch(ur -> ur.getRole().getName().name().equals(roleName));
        
        log.trace("User {} has role {}: {}", email, roleName, hasRole);
        return hasRole;
    }
    
    public Set<Role> getActiveRoles() {
        Set<Role> roles = userRoles.stream()
                .filter(UserRole::isActive)
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
        
        log.debug("User {} has {} active roles", email, roles.size());
        return roles;
    }
}