//package com.common_wealth_builders.controller;
//
//import com.common_wealth_builders.dto.response.GenericResponse;
//import com.common_wealth_builders.service.AuditService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//
//@RestController
//@RequestMapping("/v1/audit-trail")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "Audit Trail", description = "Endpoints for viewing system audit logs and user activity tracking")
//@SecurityRequirement(name = "Bearer Authentication")
//public class AuditTrailController {
//
//    private final AuditService auditTrailService;
//
//    @Operation(
//            summary = "Get all audit logs",
//            description = "Retrieves a paginated list of all system audit logs with sorting options. Only accessible by administrators."
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Audit logs retrieved successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden - Requires administrator role")
//    })
//    @GetMapping
//    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
//    public ResponseEntity<GenericResponse> getAllAuditLogs(
//            @Parameter(description = "Page number (0-based)", example = "0")
//            @RequestParam(defaultValue = "0") int page,
//
//            @Parameter(description = "Number of items per page", example = "20")
//            @RequestParam(defaultValue = "20") int size,
//
//            @Parameter(description = "Field to sort by", example = "timestamp")
//            @RequestParam(defaultValue = "timestamp") String sortBy,
//
//            @Parameter(description = "Sort direction (ASC or DESC)", example = "DESC")
//            @RequestParam(defaultValue = "DESC") String sortOrder) {
//
//        log.info("Request received: GET /audit-trail - page={}, size={}, sortBy={}, sortOrder={}",
//                page, size, sortBy, sortOrder);
//
//        Sort.Direction direction = sortOrder.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
//
//        GenericResponse response = auditTrailService.getAllAuditTrails(pageable);
//
//        log.info("Response sent: GET /audit-trail - status={}, success={}",
//                response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @Operation(
//            summary = "Get audit logs by user",
//            description = "Retrieves all audit logs for a specific user's activities"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "User audit logs retrieved successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "404", description = "User not found"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden")
//    })
//    @GetMapping("/user/{userId}")
//    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
//    public ResponseEntity<GenericResponse> getAuditLogsByUser(
//            @Parameter(description = "User ID", example = "1", required = true)
//            @PathVariable Long userId,
//
//            @Parameter(description = "Page number (0-based)", example = "0")
//            @RequestParam(defaultValue = "0") int page,
//
//            @Parameter(description = "Number of items per page", example = "20")
//            @RequestParam(defaultValue = "20") int size) {
//
//        log.info("Request received: GET /audit-trail/user/{} - userId={}, page={}, size={}",
//                userId, userId, page, size);
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
//        GenericResponse response = auditTrailService.getAuditTrailsByUserId(userId, pageable);
//
//        log.info("Response sent: GET /audit-trail/user/{} - status={}, success={}",
//                userId, response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @Operation(
//            summary = "Get audit logs by action",
//            description = "Retrieves all audit logs for a specific action type (e.g., LOGIN, CREATE_PAYMENT, etc.)"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Action audit logs retrieved successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Invalid action type"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden")
//    })
//    @GetMapping("/action/{action}")
//    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
//    public ResponseEntity<GenericResponse> getAuditLogsByAction(
//            @Parameter(description = "Action type", example = "LOGIN", required = true)
//            @PathVariable String action,
//
//            @Parameter(description = "Page number (0-based)", example = "0")
//            @RequestParam(defaultValue = "0") int page,
//
//            @Parameter(description = "Number of items per page", example = "20")
//            @RequestParam(defaultValue = "20") int size) {
//
//        log.info("Request received: GET /audit-trail/action/{} - action={}, page={}, size={}",
//                action, action, page, size);
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
//        GenericResponse response = auditTrailService.getAuditTrailsByModule(action, pageable);
//
//        log.info("Response sent: GET /audit-trail/action/{} - status={}, success={}",
//                action, response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @Operation(
//            summary = "Search audit logs by date range",
//            description = "Retrieves audit logs within a specified date range"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Audit logs retrieved successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Invalid date range"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden")
//    })
//    @GetMapping("/date-range")
//    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
//    public ResponseEntity<GenericResponse> getAuditLogsByDateRange(
//            @Parameter(description = "Start date and time", example = "2026-01-01T00:00:00", required = true)
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//
//            @Parameter(description = "End date and time", example = "2026-02-05T23:59:59", required = true)
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
//
//            @Parameter(description = "Page number (0-based)", example = "0")
//            @RequestParam(defaultValue = "0") int page,
//
//            @Parameter(description = "Number of items per page", example = "20")
//            @RequestParam(defaultValue = "20") int size) {
//
//        log.info("Request received: GET /audit-trail/date-range - startDate={}, endDate={}, page={}, size={}",
//                startDate, endDate, page, size);
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
//        GenericResponse response = auditTrailService.getAuditLogsByDateRange(startDate, endDate, pageable);
//
//        log.info("Response sent: GET /audit-trail/date-range - status={}, success={}",
//                response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @Operation(
//            summary = "Get audit log by ID",
//            description = "Retrieves detailed information about a specific audit log entry"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Audit log found and returned successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "404", description = "Audit log not found"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden")
//    })
//    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
//    public ResponseEntity<GenericResponse> getAuditLogById(
//            @Parameter(description = "Audit log ID", example = "1", required = true)
//            @PathVariable Long id) {
//
//        log.info("Request received: GET /audit-trail/{} - id={}", id, id);
//
//        GenericResponse response = auditTrailService.getAuditLogById(id);
//
//        log.info("Response sent: GET /audit-trail/{} - status={}, success={}",
//                id, response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//}