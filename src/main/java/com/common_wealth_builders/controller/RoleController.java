package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.AssignRoleRequest;
import com.common_wealth_builders.dto.request.RoleRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
@Slf4j
public class RoleController {
    
    private final RoleService roleService;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder) {
        
        log.info("Request received: GET /roles - page={}, size={}, sortBy={}, sortOrder={}", 
                page, size, sortBy, sortOrder);
        
        Sort.Direction direction = sortOrder.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        GenericResponse response = roleService.getAllRoles(pageable);
        
        log.info("Response sent: GET /roles - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> getActiveRoles() {
        log.info("Request received: GET /roles/active");
        
        GenericResponse response = roleService.getActiveRoles();
        
        log.info("Response sent: GET /roles/active - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> getRoleById(@PathVariable Long id) {
        log.info("Request received: GET /roles/{} - id={}", id, id);
        
        GenericResponse response = roleService.getRoleById(id);
        
        log.info("Response sent: GET /roles/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> createRole(@Valid @RequestBody RoleRequest request) {
        log.info("Request received: POST /roles - name={}, displayName={}", 
                request.getName(), request.getDisplayName());
        
        GenericResponse response = roleService.createRole(request);
        
        log.info("Response sent: POST /roles - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        
        log.info("Request received: PUT /roles/{} - id={}, displayName={}", 
                id, id, request.getDisplayName());
        
        GenericResponse response = roleService.updateRole(id, request);
        
        log.info("Response sent: PUT /roles/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> deleteRole(@PathVariable Long id) {
        log.info("Request received: DELETE /roles/{} - id={}", id, id);
        
        GenericResponse response = roleService.deleteRole(id);
        
        log.info("Response sent: DELETE /roles/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> activateRole(@PathVariable Long id) {
        log.info("Request received: POST /roles/{}/activate - id={}", id, id);
        
        GenericResponse response = roleService.activateRole(id);
        
        log.info("Response sent: POST /roles/{}/activate - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> deactivateRole(@PathVariable Long id) {
        log.info("Request received: POST /roles/{}/deactivate - id={}", id, id);
        
        GenericResponse response = roleService.deactivateRole(id);
        
        log.info("Response sent: POST /roles/{}/deactivate - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> searchRoles(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /roles/search - search={}, page={}, size={}", 
                search, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        GenericResponse response = roleService.searchRoles(search, pageable);
        
        log.info("Response sent: GET /roles/search - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> assignRoleToUser(@Valid @RequestBody AssignRoleRequest request) {
        log.info("Request received: POST /roles/assign - userId={}, roleId={}", 
                request.getUserId(), request.getRoleId());
        
        GenericResponse response = roleService.assignRoleToUser(request);
        
        log.info("Response sent: POST /roles/assign - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @DeleteMapping("/revoke/user/{userId}/role/{roleId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> revokeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        
        log.info("Request received: DELETE /roles/revoke/user/{}/role/{} - userId={}, roleId={}", 
                userId, roleId, userId, roleId);
        
        GenericResponse response = roleService.revokeRoleFromUser(userId, roleId);
        
        log.info("Response sent: DELETE /roles/revoke - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> getUserRoles(@PathVariable Long userId) {
        log.info("Request received: GET /roles/user/{} - userId={}", userId, userId);
        
        GenericResponse response = roleService.getUserRoles(userId);
        
        log.info("Response sent: GET /roles/user/{} - status={}, success={}", 
                userId, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/{roleId}/users")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> getRoleUsers(
            @PathVariable Long roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /roles/{}/users - roleId={}, page={}, size={}", 
                roleId, roleId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        GenericResponse response = roleService.getRoleUsers(roleId, pageable);
        
        log.info("Response sent: GET /roles/{}/users - status={}, success={}", 
                roleId, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}