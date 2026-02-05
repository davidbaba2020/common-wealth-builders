package com.common_wealth_builders.service;

import com.common_wealth_builders.dto.response.GenericResponse;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    void logAction(Long userId, String action, String module, String description);
    void logAction(Long userId, String action, String module, String description, String ipAddress, String userAgent);
    GenericResponse getAllAuditTrails(Pageable pageable);
    GenericResponse getAuditTrailsByUserId(Long userId, Pageable pageable);
    GenericResponse getAuditTrailsByModule(String module, Pageable pageable);
    GenericResponse searchAuditTrails(Long userId, String module, String action, Pageable pageable);
}