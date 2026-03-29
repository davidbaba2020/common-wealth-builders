package com.common_wealth_builders.repository;

import com.common_wealth_builders.entity.UserNotification;
import com.common_wealth_builders.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserAndIsReadFalseOrderByCreatedDateDesc(User user);
    List<UserNotification> findByUserOrderByCreatedDateDesc(User user);

    long countByUserIdAndIsReadFalse(Long userId);
}