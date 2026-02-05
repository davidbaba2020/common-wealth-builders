package com.common_wealth_builders.dto.response;

import com.common_wealth_builders.enums.NoticeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeResponse {
    
    private Long id;
    private String title;
    private String content;
    private NoticeType type;
    private boolean isPublished;
    private LocalDateTime publishDate;
    private LocalDateTime expiryDate;
    private boolean isPinned;
    private Long authorId;
    private String authorName;
    private String authorEmail;
    private String attachmentUrl;
    private Integer viewCount;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String createdBy;
    private String updatedBy;
}