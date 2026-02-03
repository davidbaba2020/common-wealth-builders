package com.common_wealth_builders.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_trails")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrail {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String module;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    private String ipAddress;
    
    private String userAgent;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @Column(nullable = false)
    private LocalDateTime updatedDate;
    
    private String createdBy;
    
    private String updatedBy;
}