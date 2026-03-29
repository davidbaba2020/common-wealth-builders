package com.common_wealth_builders.service;

import com.common_wealth_builders.dto.response.GenericResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditService {
    void logAction(Long userId, String action, String module, String description);
    void logAction(Long userId, String action, String module, String description, String ipAddress, String userAgent);
    GenericResponse getAllAuditTrails(Pageable pageable);
    GenericResponse getAuditTrailsByUserId(Long userId, Pageable pageable);
    GenericResponse getAuditTrailsByModule(String module, Pageable pageable);
    GenericResponse searchAuditTrails(Long userId, String module, String action, Pageable pageable);

    GenericResponse getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    GenericResponse getAuditLogById(Long id);

    GenericResponse searchAuditLogs(String query, Pageable pageable);
}