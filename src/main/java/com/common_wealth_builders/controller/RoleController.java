package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.AssignRoleRequest;
import com.common_wealth_builders.dto.request.CreateRoleRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "Role management - TECH_ADMIN handles role assignments")
@SecurityRequirement(name = "Bearer Authentication")
public class RoleController {
    
    private final RoleService roleService;
    
    @Operation(
            summary = "Create new role (SUPER ADMIN ONLY)",
            description = "Creates a new role in the system. Only SUPER_ADMIN can create roles."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Role created successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid role data or role already exists"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN role")
    })
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> createRole(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Role details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateRoleRequest.class))
            )
            @Valid @RequestBody CreateRoleRequest request) {
        
        log.info("Request received: POST /roles - roleName={}", request.getName());
        
        GenericResponse response = roleService.createRole(request);
        
        log.info("Response sent: POST /roles - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get all roles (TECH ADMIN)",
            description = "Retrieves all roles. TECH_ADMIN and SUPER_ADMIN can view roles."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> getAllRoles(
            @Parameter(description = "Page number", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sort field", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            
            @Parameter(description = "Sort direction", example = "ASC")
            @RequestParam(defaultValue = "ASC") String sortOrder) {
        
        log.info("Request received: GET /roles");
        
        Sort.Direction direction = sortOrder.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        GenericResponse response = roleService.getAllRoles(pageable);
        
        log.info("Response sent: GET /roles - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get role by ID (TECH ADMIN)",
            description = "Retrieves role details. TECH_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role found"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> getRoleById(
            @Parameter(description = "Role ID", example = "1", required = true)
            @PathVariable Long id) {
        
        log.info("Request received: GET /roles/{}", id);
        
        GenericResponse response = roleService.getRoleById(id);
        
        log.info("Response sent: GET /roles/{} - status={}", id, response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Update role (SUPER ADMIN ONLY)",
            description = "Updates a role. Only SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN role")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> updateRole(
            @Parameter(description = "Role ID", example = "1", required = true)
            @PathVariable Long id,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated role details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateRoleRequest.class))
            )
            @Valid @RequestBody CreateRoleRequest request) {
        
        log.info("Request received: PUT /roles/{}", id);
        
        GenericResponse response = roleService.updateRole(id, request);
        
        log.info("Response sent: PUT /roles/{} - status={}", id, response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Delete role (SUPER ADMIN ONLY)",
            description = "Deletes a role. Only SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN role")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> deleteRole(
            @Parameter(description = "Role ID", example = "5", required = true)
            @PathVariable Long id) {
        
        log.info("Request received: DELETE /roles/{}", id);
        
        GenericResponse response = roleService.deleteRole(id);
        
        log.info("Response sent: DELETE /roles/{} - status={}", id, response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Assign role to user (TECH ADMIN)",
            description = "Assigns a role to a user. TECH_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Role already assigned"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires TECH_ADMIN role")
    })
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> assignRole(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Role assignment details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AssignRoleRequest.class))
            )
            @Valid @RequestBody AssignRoleRequest request) {
        
        log.info("Request received: POST /roles/assign - userId={}, roleId={}", 
                request.getUserId(), request.getRoleId());
        
        GenericResponse response = roleService.assignRoleToUser(request);
        
        log.info("Response sent: POST /roles/assign - status={}", response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Revoke role from user (TECH ADMIN)",
            description = "Removes a role from a user. TECH_ADMIN and SUPER_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role revoked"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires TECH_ADMIN role")
    })
    @PostMapping("/revoke")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> revokeRole(
            @Parameter(description = "User ID", example = "1", required = true)
            @RequestParam Long userId,
            
            @Parameter(description = "Role ID", example = "2", required = true)
            @RequestParam Long roleId) {
        
        log.info("Request received: POST /roles/revoke - userId={}, roleId={}", userId, roleId);
        
        GenericResponse response = roleService.revokeRoleFromUser(userId, roleId);
        
        log.info("Response sent: POST /roles/revoke - status={}", response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Get user roles (TECH ADMIN)",
            description = "Gets all roles assigned to a user. TECH_ADMIN and SUPER_ADMIN."
    )
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> getUserRoles(
            @Parameter(description = "User ID", example = "1", required = true)
            @PathVariable Long userId) {
        
        log.info("Request received: GET /roles/user/{}", userId);
        
        GenericResponse response = roleService.getUserRoles(userId);
        
        log.info("Response sent: GET /roles/user/{} - status={}", userId, response.getHttpStatus());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}