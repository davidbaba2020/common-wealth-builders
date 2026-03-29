package com.common_wealth_builders.dto.response;

import com.common_wealth_builders.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserNotificationResponse {
    private Long id;
    private String title;
    private String content;
    private boolean isRead;
    private NotificationType type;
    private String createdDate;
}