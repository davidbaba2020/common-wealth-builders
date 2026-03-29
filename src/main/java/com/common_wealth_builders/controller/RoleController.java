package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.AssignRoleRequest;
import com.common_wealth_builders.dto.request.CreateRoleRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.RoleService;
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
@RequestMapping("v1/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "APIs for managing system roles")
public class RoleController {

    private final RoleService roleService;

    // 1️⃣ GET ALL (Paginated)
    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Request received: GET /roles?page={}&size={}", page, size);
        GenericResponse response = roleService.getAllRoles(page, size);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    // 2️⃣ GET ACTIVE ROLES
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
    public ResponseEntity<GenericResponse> getActiveRoles() {

        log.info("Request received: GET /roles/active");
        GenericResponse response = roleService.getActiveRoles();
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    // 3️⃣ GET BY ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> getRoleById(@PathVariable Long id) {

        log.info("Request received: GET /roles/{}", id);
        GenericResponse response = roleService.getRoleById(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    // 4️⃣ CREATE
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> createRole(
            @Valid @RequestBody CreateRoleRequest request) {

        log.info("Request received: POST /roles - roleName={}", request.getName());
        GenericResponse response = roleService.createRole(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    // 5️⃣ UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoleRequest request) {

        log.info("Request received: PUT /roles/{}", id);
        GenericResponse response = roleService.updateRole(id, request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    // 6️⃣ DELETE (Soft Delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> deleteRole(@PathVariable Long id) {

        log.info("Request received: DELETE /roles/{}", id);
        GenericResponse response = roleService.deleteRole(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    // 7️⃣ ACTIVATE
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> activateRole(@PathVariable Long id) {

        log.info("Request received: POST /roles/{}/activate", id);
        GenericResponse response = roleService.activateRole(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    // 8️⃣ DEACTIVATE
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> deactivateRole(@PathVariable Long id) {

        log.info("Request received: POST /roles/{}/deactivate", id);
        GenericResponse response = roleService.deactivateRole(id);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> searchRole(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Request received: GET /roles/search?q={}&page={}&size={}", q, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        GenericResponse response = roleService.searchRoles(q, pageable);

        return new ResponseEntity<>(response, response.getHttpStatus());
    }


    // 🔟 ASSIGN ROLE
    @PostMapping("/assign")
//    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> assignRole(
            @Valid @RequestBody AssignRoleRequest request) {

        log.info("Request received: POST /roles/assign - userId={}, roleId={}",
                request.getUserId(), request.getRoleId());

        GenericResponse response = roleService.assignRoleToUser(request);

        return new ResponseEntity<>(response, response.getHttpStatus());
    }


    // 1️⃣1️⃣ REVOKE ROLE
    @DeleteMapping("/revoke/user/{userId}/role/{roleId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> revokeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        log.info("Request received: DELETE /roles/revoke/user/{}/role/{}", userId, roleId);

        GenericResponse response = roleService.revokeRoleFromUser(userId, roleId);

        return new ResponseEntity<>(response, response.getHttpStatus());
    }


    // 1️⃣2️⃣ GET USER ROLES
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> getUserRoles(@PathVariable Long userId) {

        log.info("Request received: GET /roles/user/{}", userId);

        GenericResponse response = roleService.getUserRoles(userId);

        return new ResponseEntity<>(response, response.getHttpStatus());
    }


    // 1️⃣3️⃣ GET ROLE USERS
    @GetMapping("/{roleId}/users")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> getRoleUsers(
            @PathVariable Long roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Request received: GET /roles/{}/users?page={}&size={}", roleId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        GenericResponse response = roleService.getRoleUsers(roleId, pageable);

        return new ResponseEntity<>(response, response.getHttpStatus());
    }

}