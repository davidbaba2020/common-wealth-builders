package com.common_wealth_builders.dto.request;

import com.common_wealth_builders.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserNotificationRequest {
    private Long userId;
    private String title;
    private String content;
    private NotificationType type;
}