package com.common_wealth_builders.entity;

import com.common_wealth_builders.entity.base.BaseEntity;
import com.common_wealth_builders.enums.NoticeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "notices", indexes = {
        @Index(name = "idx_notice_type", columnList = "type"),
        @Index(name = "idx_notice_published", columnList = "isPublished"),
        @Index(name = "idx_notice_publish_date", columnList = "publishDate"),
        @Index(name = "idx_notice_author", columnList = "author_id")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Notice extends BaseEntity {
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NoticeType type;
    
    @Column(nullable = false)
    private boolean isPublished = false;
    
    @Column
    private LocalDateTime publishDate;
    
    @Column
    private LocalDateTime expiryDate;
    
    @Column(nullable = false)
    private boolean isPinned = false;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @Column(columnDefinition = "TEXT")
    private String attachmentUrl;
    
    @Column(nullable = false)
    private Integer viewCount = 0;

    // Track which users have read this notice
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "notice_reads",
            joinColumns = @JoinColumn(name = "notice_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> readByUsers = new HashSet<>();

    // ✅ Mark notice as read by a user
    public void markAsRead(User user) {
        if (user != null) {
            this.readByUsers.add(user);
        }
    }

    // ✅ Check if a notice is read by a specific user
    public boolean isReadBy(User user) {
        return user != null && readByUsers.contains(user);
    }

    
    @PrePersist
    protected void onCreate() {
        log.info("Creating new notice: title={}, type={}, author={}", 
                 title, type, author != null ? author.getEmail() : null);
        validateNotice();
    }
    
    @PreUpdate
    protected void onUpdate() {
        log.debug("Updating notice: id={}, title={}", getId(), title);
        validateNotice();
    }
    
    private void validateNotice() {
        if (title == null || title.trim().isEmpty()) {
            log.error("Notice title is required");
            throw new IllegalArgumentException("Notice title is required");
        }
        
        if (content == null || content.trim().isEmpty()) {
            log.error("Notice content is required");
            throw new IllegalArgumentException("Notice content is required");
        }
        
        log.trace("Notice validation passed");
    }
    
    public void publish() {
        log.info("Publishing notice: id={}, title={}", getId(), title);
        
        if (this.isPublished) {
            log.warn("Notice already published: id={}", getId());
            throw new IllegalStateException("Notice is already published");
        }
        
        this.isPublished = true;
        this.publishDate = LocalDateTime.now();
        
        log.info("Notice published successfully: id={}", getId());
    }
    
    public void unpublish() {
        log.info("Unpublishing notice: id={}, title={}", getId(), title);
        
        this.isPublished = false;
        this.publishDate = null;
        
        log.info("Notice unpublished: id={}", getId());
    }
    
    public void pin() {
        log.info("Pinning notice: id={}, title={}", getId(), title);
        this.isPinned = true;
    }
    
    public void unpin() {
        log.info("Unpinning notice: id={}, title={}", getId(), title);
        this.isPinned = false;
    }
    
    public void incrementViewCount() {
        this.viewCount++;
        log.debug("View count incremented for notice: id={}, count={}", getId(), viewCount);
    }
    
    public boolean isActive() {
        if (!isPublished) {
            return false;
        }

//        if (expiryDate != null && LocalDateTime.now().isAfter(expiryDate)) {
//            return false;
//        }
        
        return !isDeleted();
    }
}