package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/audit-trail")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit Trail", description = "System audit logs - TECH_ADMIN monitors system activity")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditTrailController {
    
    private final AuditService auditService;
    
    @Operation(
            summary = "Get all audit logs (TECH ADMIN)",
            description = "Retrieves all system audit logs. Only TECH_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Audit logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires TECH_ADMIN role")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> getAllAuditLogs(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Field to sort by", example = "createdDate")
            @RequestParam(defaultValue = "createdDate") String sortBy,
            
            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortOrder) {
        
        log.info("Request received: GET /audit-trail - page={}, size={}, sortBy={}, sortOrder={}", 
                page, size, sortBy, sortOrder);
        
        Sort.Direction direction = sortOrder.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        GenericResponse response = auditService.getAllAuditTrails(pageable);
        
        log.info("Response sent: GET /audit-trail - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get audit logs by user (TECH ADMIN)",
            description = "Retrieves all audit logs for a specific user. Only TECH_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User audit logs retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> getAuditLogsByUser(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long userId,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Request received: GET /audit-trail/user/{} - userId={}, page={}, size={}", 
                userId, userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        GenericResponse response = auditService.getAuditTrailsByUserId(userId, pageable);
        
        log.info("Response sent: GET /audit-trail/user/{} - status={}, success={}", 
                userId, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get audit logs by module (TECH ADMIN)",
            description = "Retrieves logs for a specific module (e.g., PAYMENT, USER, EXPENSE). Only TECH_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Module audit logs retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid module")
    })
    @GetMapping("/module/{module}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> getAuditLogsByModule(
            @Parameter(description = "Module name", example = "PAYMENT", required = true)
            @PathVariable String module,
            
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Request received: GET /audit-trail/module/{} - module={}, page={}, size={}", 
                module, module, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        GenericResponse response = auditService.getAuditTrailsByModule(module, pageable);
        
        log.info("Response sent: GET /audit-trail/module/{} - status={}, success={}", 
                module, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Search audit logs (TECH ADMIN)",
            description = "Search audit logs with filters. Only TECH_ADMIN and SUPER_ADMIN."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> searchAuditLogs(
            @Parameter(description = "User ID filter")
            @RequestParam(required = false) Long userId,
            
            @Parameter(description = "Module filter")
            @RequestParam(required = false) String module,
            
            @Parameter(description = "Action filter")
            @RequestParam(required = false) String action,
            
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Items per page", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Request received: GET /audit-trail/search - userId={}, module={}, action={}", 
                userId, module, action);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        GenericResponse response = auditService.searchAuditTrails(userId, module, action, pageable);
        
        log.info("Response sent: GET /audit-trail/search - status={}", response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}