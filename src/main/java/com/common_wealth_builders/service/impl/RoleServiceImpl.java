package com.common_wealth_builders.service.impl;


import com.common_wealth_builders.dto.request.AssignRoleRequest;
import com.common_wealth_builders.dto.request.CreateRoleRequest;
import com.common_wealth_builders.dto.request.UserNotificationRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.dto.response.PageResponse;
import com.common_wealth_builders.dto.response.RoleResponse;
import com.common_wealth_builders.dto.response.UserRoleResponse;
import com.common_wealth_builders.entity.Role;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.entity.UserRole;
import com.common_wealth_builders.enums.NotificationType;
import com.common_wealth_builders.exception.ResourceNotFoundException;
import com.common_wealth_builders.repository.RoleRepository;
import com.common_wealth_builders.repository.UserRepository;
import com.common_wealth_builders.repository.UserRoleRepository;
import com.common_wealth_builders.service.AuditService;
import com.common_wealth_builders.service.RoleService;
import com.common_wealth_builders.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuditService auditService;
    private final UserNotificationService userNotificationService;

    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository, UserRoleRepository userRoleRepository, AuditService auditService, UserNotificationService userNotificationService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.auditService = auditService;
        this.userNotificationService = userNotificationService;
    }

    @Override
    public GenericResponse getAllRoles(int page, int size) {

        Page<Role> roles = roleRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdDate").descending()));

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Roles fetched successfully")
                .httpStatus(HttpStatus.OK)
                .data(roles.map(this::mapToResponse))
                .build();
    }

    @Override
    public GenericResponse createRole(CreateRoleRequest request) {

        if (roleRepository.existsByName(request.getName())) {
            return new GenericResponse(false,
                    "Role already exists",
                    HttpStatus.BAD_REQUEST);
        }

        Role role = Role.builder()
                .name(request.getName())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .build();

        roleRepository.save(role);

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Role created successfully")
                .httpStatus(HttpStatus.CREATED)
                .data(mapToResponse(role))
                .build();
    }

    @Override
    public GenericResponse deleteRole(Long id) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.softDelete("SYSTEM");

        return new GenericResponse(true,
                "Role deleted successfully",
                HttpStatus.OK);
    }

    @Override
    public GenericResponse activateRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.activate();
        return new GenericResponse(true, "Role activated", HttpStatus.OK);
    }

    @Override
    public GenericResponse deactivateRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        role.deactivate();
        return new GenericResponse(true, "Role deactivated", HttpStatus.OK);
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .isActive(role.isActive())
                .code(role.getCode())
                .build();
    }
    
    @Override
    public GenericResponse getActiveRoles() {
        log.info("Fetching all active roles");
        
        List<Role> activeRoles = roleRepository.findAllActiveRoles();
        
        List<RoleResponse> roleResponses = activeRoles.stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());
        
        log.info("Successfully fetched {} active roles", roleResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Active roles retrieved successfully")
                .data(roleResponses)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getRoleById(Long id) {
        log.info("Fetching role by ID: {}", id);
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Role not found with ID: {}", id);
                    return new ResourceNotFoundException("Role not found with ID: " + id);
                });
        
        log.info("Successfully fetched role: name={}", role.getName());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Role retrieved successfully")
                .data(mapToRoleResponse(role))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional
    public GenericResponse updateRole(Long id, CreateRoleRequest request) {
        log.info("Updating role: id={}, newDisplayName={}", id, request.getDisplayName());
        
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Role not found with ID: {}", id);
                    return new ResourceNotFoundException("Role not found with ID: " + id);
                });
        
        if (role.isSystemRole()) {
            log.warn("Attempt to update system role blocked: id={}, name={}", id, role.getName());
            throw new IllegalStateException("Cannot update system role");
        }
        
        role.setDisplayName(request.getDisplayName());
        role.setDescription(request.getDescription());
        if (request.getIsActive() != null) {
            role.setActive(request.getIsActive());
        }
        
        Role updatedRole = roleRepository.save(role);

        auditService.logAction(
                getCurrentUserId(),
                "ROLE_UPDATED",
                "ROLES",
                "Role updated: " + updatedRole.getName()
        );
        
        log.info("Successfully updated role: id={}, name={}", updatedRole.getId(), updatedRole.getName());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Role updated successfully")
                .data(mapToRoleResponse(updatedRole))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
//    @Override
//    @Transactional
//    public GenericResponse deleteRole(Long id) {
//        log.info("Deleting role: id={}", id);
//
//        Role role = roleRepository.findById(id)
//                .orElseThrow(() -> {
//                    log.error("Role not found with ID: {}", id);
//                    return new ResourceNotFoundException("Role not found with ID: " + id);
//                });
//
//        if (role.isSystemRole()) {
//            log.warn("Attempt to delete system role blocked: id={}, name={}", id, role.getName());
//            throw new IllegalStateException("Cannot delete system role");
//        }
//
//        Long usersWithRole = userRoleRepository.countActiveUsersByRoleId(id);
//        if (usersWithRole > 0) {
//            log.warn("Cannot delete role with active users: roleId={}, userCount={}", id, usersWithRole);
//            throw new IllegalStateException("Cannot delete role assigned to " + usersWithRole + " users");
//        }
//
//        String currentUser = getCurrentUserEmail();
//        role.softDelete(currentUser);
//        roleRepository.save(role);
//
////        auditService.logAction(
////                getCurrentUserId(),
////                "ROLE_DELETED",
////                "ROLES",
////                "Role deleted: " + role.getName()
////        );
//
//        log.info("Successfully deleted role: id={}, name={}", id, role.getName());
//
//        return GenericResponse.builder()
//                .isSuccess(true)
//                .message("Role deleted successfully")
//                .httpStatus(HttpStatus.OK)
//                .build();
//    }
//
//    @Override
//    @Transactional
//    public GenericResponse activateRole(Long id) {
//        log.info("Activating role: id={}", id);
//
//        Role role = roleRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
//
//        role.activate();
//        roleRepository.save(role);
//
////        auditService.logAction(
////                getCurrentUserId(),
////                "ROLE_ACTIVATED",
////                "ROLES",
////                "Role activated: " + role.getName()
////        );
//
//        log.info("Successfully activated role: id={}, name={}", id, role.getName());
//
//        return GenericResponse.builder()
//                .isSuccess(true)
//                .message("Role activated successfully")
//                .data(mapToRoleResponse(role))
//                .httpStatus(HttpStatus.OK)
//                .build();
//    }
//
//    @Override
//    @Transactional
//    public GenericResponse deactivateRole(Long id) {
//        log.info("Deactivating role: id={}", id);
//
//        Role role = roleRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
//
//        role.deactivate();
//        roleRepository.save(role);
////
////        auditService.logAction(
////                getCurrentUserId(),
////                "ROLE_DEACTIVATED",
////                "ROLES",
////                "Role deactivated: " + role.getName()
////        );
//
//        log.info("Successfully deactivated role: id={}, name={}", id, role.getName());
//
//        return GenericResponse.builder()
//                .isSuccess(true)
//                .message("Role deactivated successfully")
//                .data(mapToRoleResponse(role))
//                .httpStatus(HttpStatus.OK)
//                .build();
//    }
@Override
public GenericResponse searchRoles(String search, Pageable pageable) {
        log.info("Searching roles with term: {}", search);
        
        Page<Role> rolesPage = roleRepository.searchRoles(search, pageable);
        
        List<RoleResponse> roleResponses = rolesPage.getContent().stream()
                .map(this::mapToRoleResponse)
                .collect(Collectors.toList());

        PageResponse<RoleResponse> pageResponse = PageResponse.<RoleResponse>builder()
                .content(roleResponses)
                .pageNumber(rolesPage.getNumber())
                .pageSize(rolesPage.getSize())
                .totalElements(rolesPage.getTotalElements())
                .totalPages(rolesPage.getTotalPages())
                .last(rolesPage.isLast())
                .first(rolesPage.isFirst())
                .build();

        log.info("Search completed: found {} roles", roleResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Search completed successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Transactional
    @Override
    public GenericResponse assignRoleToUser(AssignRoleRequest request) {
        log.info("Assigning role to user: userId={}, roleId={}", request.getUserId(), request.getRoleId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        String currentUser = getCurrentUserEmail();

        Optional<UserRole> existingUserRole = userRoleRepository.findByUserAndRole(user, role);
        if (existingUserRole.isPresent()) {
            UserRole userRole = existingUserRole.get();
            if (userRole.isActive()) {
                throw new IllegalStateException("Role already assigned to user");
            }
            // Reactivate
            userRole.setActive(true);
            userRole.setAssignedDate(LocalDateTime.now());
            userRole.setAssignedBy(currentUser);
            userRole.setRemarks(request.getRemarks());
            userRole.setRevokedDate(null);
            userRole.setRevokedBy(null);
            // No need to save explicitly, will be updated at flush
            log.info("Reactivated role {} for user {}", role.getName(), user.getEmail());
        } else {
            // Create new
            UserRole newUserRole = UserRole.builder()
                    .user(user)
                    .role(role)
                    .assignedDate(LocalDateTime.now())
                    .assignedBy(currentUser)
                    .remarks(request.getRemarks())
                    .isActive(true)
                    .build();
            userRoleRepository.save(newUserRole);
            // Also add to user's set to keep in-memory consistent (optional)
            user.getUserRoles().add(newUserRole);
            log.info("Assigned new role {} to user {}", role.getName(), user.getEmail());
        }

        auditService.logAction(
                user.getId(),
                "ROLE_ASSIGNED",
                "USER_ROLES",
                String.format("Role %s assigned to user %s", role.getName(), user.getEmail())
        );
        userNotificationService.createNotification(UserNotificationRequest.builder()
                .userId(user.getId())
                .title("Role assigned")
                .content("You have been assigned " + role.getName() + "...")
                .type(NotificationType.ROLE_ASSIGNED)
                .build());
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Role assigned successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Transactional
    @Override
    public GenericResponse revokeRoleFromUser(Long userId, Long roleId) {
        log.info("Revoking role from user: userId={}, roleId={}", userId, roleId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        
        String currentUser = getCurrentUserEmail();
        user.revokeRole(role, currentUser);
        userRepository.save(user);
        
        auditService.logAction(
                userId,
                "ROLE_REVOKED",
                "USER_ROLES",
                String.format("Role %s revoked from user %s", role.getName(), user.getEmail())
        );
        
        log.info("Successfully revoked role {} from user {}", role.getName(), user.getEmail());
        userNotificationService.createNotification(UserNotificationRequest.builder()
                .userId(user.getId())
                .title("Role revoked")
                .content("Your role as " + role.getName() + "has been removed...")
                .type(NotificationType.ROLE_REMOVED)
                .build());
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Role revoked successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getUserRoles(Long userId) {
        log.info("Fetching roles for user: userId={}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<UserRoleResponse> userRoleResponses = user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .map(this::mapToUserRoleResponse)
                .collect(Collectors.toList());
        
        log.info("Found {} active roles for user {}", userRoleResponses.size(), user.getEmail());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("User roles retrieved successfully")
                .data(userRoleResponses)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getRoleUsers(Long roleId, Pageable pageable) {
        log.info("Fetching users for role: roleId={}", roleId);
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        
        List<UserRole> userRoles = userRoleRepository.findByRoleIdAndIsActiveTrue(roleId);
        
        List<UserRoleResponse> responses = userRoles.stream()
                .map(this::mapToUserRoleResponse)
                .collect(Collectors.toList());
        
        log.info("Found {} users with role {}", responses.size(), role.getName());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Role users retrieved successfully")
                .data(responses)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    private RoleResponse mapToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName())
                .description(role.getDescription())
                .isActive(role.isActive())
                .isSystemRole(role.isSystemRole())
                .code(role.getCode())
                .createdDate(role.getCreatedDate())
                .updatedDate(role.getUpdatedDate())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .version(role.getVersion())
                .build();
    }
    
    private UserRoleResponse mapToUserRoleResponse(UserRole userRole) {
        return UserRoleResponse.builder()
                .id(userRole.getId())
                .userId(userRole.getUser().getId())
                .userEmail(userRole.getUser().getEmail())
                .userFullName(userRole.getUser().getFirstname() + " " + userRole.getUser().getLastname())
                .roleId(userRole.getRole().getId())
                .roleName(userRole.getRole().getName().name())
                .roleDisplayName(userRole.getRole().getDisplayName())
                .assignedDate(userRole.getAssignedDate())
                .assignedBy(userRole.getAssignedBy())
                .revokedDate(userRole.getRevokedDate())
                .revokedBy(userRole.getRevokedBy())
                .isActive(userRole.isActive())
                .remarks(userRole.getRemarks())
                .build();
    }
    
    private String getCurrentUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            log.warn("Could not get current user email, using SYSTEM", e);
            return "SYSTEM";
        }
    }
    
    private Long getCurrentUserId() {
        try {
            String email = getCurrentUserEmail();
            return userRepository.findByEmail(email)
                    .map(User::getId)
                    .orElse(1L);
        } catch (Exception e) {
            log.warn("Could not get current user ID, using 1", e);
            return 1L;
        }
    }
}