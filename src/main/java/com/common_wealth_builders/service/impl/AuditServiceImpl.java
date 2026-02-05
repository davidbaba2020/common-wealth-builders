package com.common_wealth_builders.service.impl;

import com.common_wealth_builders.dto.response.AuditTrailResponse;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.dto.response.PageResponse;
import com.common_wealth_builders.entity.AuditTrail;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.exception.ResourceNotFoundException;
import com.common_wealth_builders.repository.AuditTrailRepository;
import com.common_wealth_builders.repository.UserRepository;
import com.common_wealth_builders.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {
    
    private final AuditTrailRepository auditTrailRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public void logAction(Long userId, String action, String module, String description) {
        logAction(userId, action, module, description, null, null);
    }
    
    @Override
    @Transactional
    public void logAction(Long userId, String action, String module, String description, 
                         String ipAddress, String userAgent) {
        try {
            log.debug("Logging audit action: userId={}, action={}, module={}", userId, action, module);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
            
            AuditTrail auditTrail = AuditTrail.builder()
                    .user(user)
                    .action(action)
                    .module(module)
                    .description(description)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .createdDate(LocalDateTime.now())
                    .updatedDate(LocalDateTime.now())
                    .createdBy(user.getEmail())
                    .updatedBy(user.getEmail())
                    .build();
            
            auditTrailRepository.save(auditTrail);
            
            log.trace("Audit trail logged successfully: action={}, module={}", action, module);
            
        } catch (Exception e) {
            log.error("Failed to log audit trail: userId={}, action={}, module={}", 
                     userId, action, module, e);
        }
    }
    
    @Override
    public GenericResponse getAllAuditTrails(Pageable pageable) {
        log.info("Fetching all audit trails with pagination");
        
        Page<AuditTrail> auditTrailsPage = auditTrailRepository.findAll(pageable);
        
        List<AuditTrailResponse> auditTrailResponses = auditTrailsPage.getContent().stream()
                .map(this::mapToAuditTrailResponse)
                .collect(Collectors.toList());
        
        PageResponse<AuditTrailResponse> pageResponse = PageResponse.<AuditTrailResponse>builder()
                .content(auditTrailResponses)
                .pageNumber(auditTrailsPage.getNumber())
                .pageSize(auditTrailsPage.getSize())
                .totalElements(auditTrailsPage.getTotalElements())
                .totalPages(auditTrailsPage.getTotalPages())
                .last(auditTrailsPage.isLast())
                .first(auditTrailsPage.isFirst())
                .build();
        
        log.info("Successfully fetched {} audit trails", auditTrailResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Audit trails retrieved successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getAuditTrailsByUserId(Long userId, Pageable pageable) {
        log.info("Fetching audit trails for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Page<AuditTrail> auditTrailsPage = auditTrailRepository.findByUserId(userId, pageable);
        
        List<AuditTrailResponse> auditTrailResponses = auditTrailsPage.getContent().stream()
                .map(this::mapToAuditTrailResponse)
                .collect(Collectors.toList());
        
        PageResponse<AuditTrailResponse> pageResponse = PageResponse.<AuditTrailResponse>builder()
                .content(auditTrailResponses)
                .pageNumber(auditTrailsPage.getNumber())
                .pageSize(auditTrailsPage.getSize())
                .totalElements(auditTrailsPage.getTotalElements())
                .totalPages(auditTrailsPage.getTotalPages())
                .last(auditTrailsPage.isLast())
                .first(auditTrailsPage.isFirst())
                .build();
        
        log.info("Found {} audit trails for user: {}", auditTrailResponses.size(), user.getEmail());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("User audit trails retrieved successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getAuditTrailsByModule(String module, Pageable pageable) {
        log.info("Fetching audit trails for module: {}", module);
        
        Page<AuditTrail> auditTrailsPage = auditTrailRepository.findByModule(module, pageable);
        
        List<AuditTrailResponse> auditTrailResponses = auditTrailsPage.getContent().stream()
                .map(this::mapToAuditTrailResponse)
                .collect(Collectors.toList());
        
        PageResponse<AuditTrailResponse> pageResponse = PageResponse.<AuditTrailResponse>builder()
                .content(auditTrailResponses)
                .pageNumber(auditTrailsPage.getNumber())
                .pageSize(auditTrailsPage.getSize())
                .totalElements(auditTrailsPage.getTotalElements())
                .totalPages(auditTrailsPage.getTotalPages())
                .last(auditTrailsPage.isLast())
                .first(auditTrailsPage.isFirst())
                .build();
        
        log.info("Found {} audit trails for module: {}", auditTrailResponses.size(), module);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Module audit trails retrieved successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse searchAuditTrails(Long userId, String module, String action, Pageable pageable) {
        log.info("Searching audit trails: userId={}, module={}, action={}", userId, module, action);
        
        Page<AuditTrail> auditTrailsPage = auditTrailRepository.searchAuditTrails(
                userId, module, action, pageable);
        
        List<AuditTrailResponse> auditTrailResponses = auditTrailsPage.getContent().stream()
                .map(this::mapToAuditTrailResponse)
                .collect(Collectors.toList());
        
        PageResponse<AuditTrailResponse> pageResponse = PageResponse.<AuditTrailResponse>builder()
                .content(auditTrailResponses)
                .pageNumber(auditTrailsPage.getNumber())
                .pageSize(auditTrailsPage.getSize())
                .totalElements(auditTrailsPage.getTotalElements())
                .totalPages(auditTrailsPage.getTotalPages())
                .last(auditTrailsPage.isLast())
                .first(auditTrailsPage.isFirst())
                .build();
        
        log.info("Search completed: found {} audit trails", auditTrailResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Search completed successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    private AuditTrailResponse mapToAuditTrailResponse(AuditTrail auditTrail) {
        return AuditTrailResponse.builder()
                .id(auditTrail.getId())
                .userId(auditTrail.getUser().getId())
                .userEmail(auditTrail.getUser().getEmail())
                .userFullName(auditTrail.getUser().getFirstname() + " " + auditTrail.getUser().getLastname())
                .action(auditTrail.getAction())
                .module(auditTrail.getModule())
                .description(auditTrail.getDescription())
                .ipAddress(auditTrail.getIpAddress())
                .userAgent(auditTrail.getUserAgent())
                .createdDate(auditTrail.getCreatedDate())
                .createdBy(auditTrail.getCreatedBy())
                .build();
    }
}