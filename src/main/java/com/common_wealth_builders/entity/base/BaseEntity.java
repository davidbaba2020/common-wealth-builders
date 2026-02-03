package com.common_wealth_builders.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedDate;
    
    @CreatedBy
    @Column(length = 100, updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    @Column(length = 100)
    private String updatedBy;
    
    @Version
    @Column(nullable = false)
    private Long version;
    
    @Column(nullable = false)
    private boolean isDeleted = false;
    
    @Column
    private LocalDateTime deletedDate;
    
    @Column(length = 100)
    private String deletedBy;
    
    public void softDelete(String deletedBy) {
        this.isDeleted = true;
        this.deletedDate = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
    
    public void restore() {
        this.isDeleted = false;
        this.deletedDate = null;
        this.deletedBy = null;
    }
    
    public boolean isActive() {
        return !isDeleted;
    }
}