package com.common_wealth_builders.service.impl;

import com.common_wealth_builders.dto.request.UserNotificationRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.dto.response.UserNotificationResponse;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.entity.UserNotification;
import com.common_wealth_builders.repository.UserNotificationRepository;
import com.common_wealth_builders.repository.UserRepository;
import com.common_wealth_builders.service.UserNotificationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public GenericResponse createNotification(UserNotificationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserNotification notification = UserNotification.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .type(request.getType())
                .isRead(false)
                .build();

        userNotificationRepository.save(notification);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notification created")
                .data(notification.getId())
                .httpStatus(org.springframework.http.HttpStatus.CREATED)
                .build();
    }

    @Override
    public GenericResponse  getUserNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserNotificationResponse> userNotificationResponseList = userNotificationRepository.findByUserOrderByCreatedDateDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notification created")
                .data(userNotificationResponseList)
                .httpStatus(org.springframework.http.HttpStatus.CREATED)
                .build();
    }

    @Override
    public GenericResponse getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<UserNotificationResponse> userNotificationResponseList =  userNotificationRepository.findByUserAndIsReadFalseOrderByCreatedDateDesc(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notification created")
                .data(userNotificationResponseList)
                .httpStatus(org.springframework.http.HttpStatus.CREATED)
                .build();
    }

    @Override
    public GenericResponse getUnreadCount(Long userId) {

        long count = userNotificationRepository.countByUserIdAndIsReadFalse(userId);

        Map<String, Long> response = new HashMap<>();
        response.put("unread", count);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Unread count fetched successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional
    public GenericResponse markAsRead(Long id) {
        UserNotification notification = userNotificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);
        userNotificationRepository.save(notification);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Notification marked as read")
                .data(notification.getId())
                .httpStatus(org.springframework.http.HttpStatus.OK)
                .build();
    }

    private UserNotificationResponse mapToResponse(UserNotification userNotification) {
        return UserNotificationResponse.builder()
                .id(userNotification.getId())
                .title(userNotification.getTitle())
                .content(userNotification.getContent())
                .type(userNotification.getType())
                .isRead(userNotification.isRead())
                .createdDate(userNotification.getCreatedDate().format(formatter))
                .build();
    }
}