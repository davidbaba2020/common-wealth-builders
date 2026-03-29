package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.UserNotificationRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/user-notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    @PostMapping
    public GenericResponse create(@RequestBody UserNotificationRequest request) {
        return userNotificationService.createNotification(request);
    }

    @GetMapping("/{userId}")
    public GenericResponse getUserNotifications(@PathVariable Long userId) {
        return userNotificationService.getUserNotifications(userId);
    }

    @GetMapping("/{userId}/unread-count")
    public GenericResponse getUnreadCount(@PathVariable Long userId) {
        return userNotificationService.getUnreadCount(userId);
    }

    @GetMapping("/{userId}/unread")
    public GenericResponse getUnreadNotifications(@PathVariable Long userId) {
        return userNotificationService.getUnreadNotifications(userId);
    }

    @PutMapping("/{id}/mark-read")
    public GenericResponse markAsRead(@PathVariable Long id) {
        return userNotificationService.markAsRead(id);
    }
}