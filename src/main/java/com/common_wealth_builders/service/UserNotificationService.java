package com.common_wealth_builders.service;

import com.common_wealth_builders.dto.request.UserNotificationRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.dto.response.UserNotificationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserNotificationService {
    GenericResponse createNotification(UserNotificationRequest request);
    GenericResponse getUserNotifications(Long userId);
    GenericResponse getUnreadNotifications(Long userId);
    GenericResponse markAsRead(Long id);

    GenericResponse getUnreadCount(Long userId);
}