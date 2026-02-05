//package com.common_wealth_builders.controller;
//
//import com.common_wealth_builders.dto.request.AssignRoleRequest;
//import com.common_wealth_builders.dto.request.CreateRoleRequest;
//import com.common_wealth_builders.dto.response.GenericResponse;
//import com.common_wealth_builders.service.RoleService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/v1/roles")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "Role Management", description = "Endpoints for managing user roles and permissions")
//@SecurityRequirement(name = "Bearer Authentication")
//public class RoleController {
//
//    private final RoleService roleService;
//
//    @Operation(
//            summary = "Create new role",
//            description = "Creates a new role in the system with specified permissions. Only SUPER_ADMIN can create roles."
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "201",
//                    description = "Role created successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "400", description = "Invalid role data or role already exists"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN role")
//    })
//    @PostMapping
//    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
//    public ResponseEntity<GenericResponse> createRole(
//            @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "Role details including name and description",
//                    required = true,
//                    content = @Content(schema = @Schema(implementation = CreateRoleRequest.class))
//            )
//            @Valid @RequestBody CreateRoleRequest request,
//            Authentication authentication) {
//
//        log.info("Request received: POST /roles - roleName={}, createdBy={}",
//                request.getName(), authentication.getName());
//
//        GenericResponse response = roleService.createRole(request);
//
//        log.info("Response sent: POST /roles - status={}, success={}",
//                response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @Operation(
//            summary = "Get all roles",
//            description = "Retrieves a list of all roles in the system"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Roles retrieved successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden")
//    })
//    @GetMapping
//    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
//    public ResponseEntity<GenericResponse> getAllRoles() {
//        log.info("Request received: GET /roles");
//        Sort.Direction direction = sortOrder.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
//        GenericResponse response = roleService.getAllRoles();
//
//        log.info("Response sent: GET /roles - status={}, success={}",
//                response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @Operation(
//            summary = "Get role by ID",
//            description = "Retrieves detailed information about a specific role including its permissions"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Role found and returned successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "404", description = "Role not found"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden")
//    })
//    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
//    public ResponseEntity<GenericResponse> getRoleById(
//            @Parameter(description = "Role ID", example = "1", required = true)
//            @PathVariable Long id) {
//
//        log.info("Request received: GET /roles/{} - id={}", id, id);
//
//        GenericResponse response = roleService.getRoleById(id);
//
//        log.info("Response sent: GET /roles/{} - status={}, success={}",
//                id, response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @Operation(
//            summary = "Update role",
//            description = "Updates an existing role's details and permissions"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Role updated successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "404", description = "Role not found"),
//            @ApiResponse(responseCode = "400", description = "Invalid role data or cannot update system role"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN role")
//    })
//    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
//    public ResponseEntity<GenericResponse> updateRole(
//            @Parameter(description = "Role ID to update", example = "1", required = true)
//            @PathVariable Long id,
//
//            @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "Updated role details",
//                    required = true,
//                    content = @Content(schema = @Schema(implementation = CreateRoleRequest.class))
//            )
//            @Valid @RequestBody CreateRoleRequest request,
//            Authentication authentication) {
//
//        log.info("Request received: PUT /roles/{} - id={}, updatedBy={}",
//                id, id, authentication.getName());
//
//        GenericResponse response = roleService.updateRole(id, request, authentication.getName());
//
//        log.info("Response sent: PUT /roles/{} - status={}, success={}",
//                id, response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @Operation(
//            summary = "Delete role",
//            description = "Permanently deletes a role. System roles cannot be deleted. Users must be unassigned from the role first."
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Role deleted successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "404", description = "Role not found"),
//            @ApiResponse(responseCode = "400", description = "Cannot delete system role or role is still assigned to users"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN role")
//    })
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
//    public ResponseEntity<GenericResponse> deleteRole(
//            @Parameter(description = "Role ID to delete", example = "5", required = true)
//            @PathVariable Long id,
//            Authentication authentication) {
//
//        log.info("Request received: DELETE /roles/{} - id={}, deletedBy={}",
//                id, id, authentication.getName());
//
//        GenericResponse response = roleService.deleteRole(id, authentication.getName());
//
//        log.info("Response sent: DELETE /roles/{} - status={}, success={}",
//                id, response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @Operation(
//            summary = "Assign role to user",
//            description = "Assigns a role to a user, granting them the associated permissions"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Role assigned successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "404", description = "User or role not found"),
//            @ApiResponse(responseCode = "400", description = "Role already assigned to user"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN or TECH_ADMIN role")
//    })
//    @PostMapping("/assign")
//    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
//    public ResponseEntity<GenericResponse> assignRole(
//            @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "User and role assignment details",
//                    required = true,
//                    content = @Content(schema = @Schema(implementation = AssignRoleRequest.class))
//            )
//            @Valid @RequestBody AssignRoleRequest request,
//            Authentication authentication) {
//
//        log.info("Request received: POST /roles/assign - userId={}, roleId={}, assignedBy={}",
//                request.getUserId(), request.getRoleId(), authentication.getName());
//
//        GenericResponse response = roleService.assignRole(request, authentication.getName());
//
//        log.info("Response sent: POST /roles/assign - status={}, success={}",
//                response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @Operation(
//            summary = "Revoke role from user",
//            description = "Removes a role assignment from a user, revoking the associated permissions"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "Role revoked successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "404", description = "User, role, or assignment not found"),
//            @ApiResponse(responseCode = "400", description = "Cannot revoke user's only role or system role"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden - Requires SUPER_ADMIN or TECH_ADMIN role")
//    })
//    @PostMapping("/revoke")
//    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
//    public ResponseEntity<GenericResponse> revokeRole(
//            @io.swagger.v3.oas.annotations.parameters.RequestBody(
//                    description = "User and role revocation details",
//                    required = true,
//                    content = @Content(schema = @Schema(implementation = AssignRoleRequest.class))
//            )
//            @Valid @RequestBody AssignRoleRequest request,
//            Authentication authentication) {
//
//        log.info("Request received: POST /roles/revoke - userId={}, roleId={}, revokedBy={}",
//                request.getUserId(), request.getRoleId(), authentication.getName());
//
//        GenericResponse response = roleService.revokeRole(request, authentication.getName());
//
//        log.info("Response sent: POST /roles/revoke - status={}, success={}",
//                response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//
//    @Operation(
//            summary = "Get user roles",
//            description = "Retrieves all roles assigned to a specific user"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(
//                    responseCode = "200",
//                    description = "User roles retrieved successfully",
//                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
//            ),
//            @ApiResponse(responseCode = "404", description = "User not found"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized"),
//            @ApiResponse(responseCode = "403", description = "Forbidden")
//    })
//    @GetMapping("/user/{userId}")
//    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
//    public ResponseEntity<GenericResponse> getUserRoles(
//            @Parameter(description = "User ID", example = "1", required = true)
//            @PathVariable Long userId) {
//
//        log.info("Request received: GET /roles/user/{} - userId={}", userId, userId);
//
//        GenericResponse response = roleService.getUserRoles(userId);
//
//        log.info("Response sent: GET /roles/user/{} - status={}, success={}",
//                userId, response.getHttpStatus(), response.isSuccess());
//
//        return new ResponseEntity<>(response, response.getHttpStatus());
//    }
//}