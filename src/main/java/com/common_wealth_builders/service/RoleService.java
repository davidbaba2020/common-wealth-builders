package com.common_wealth_builders.service;

import com.common_wealth_builders.dto.request.AssignRoleRequest;
import com.common_wealth_builders.dto.request.CreateRoleRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface RoleService {
    GenericResponse getAllRoles(Pageable pageable);
    GenericResponse getActiveRoles();
    GenericResponse getRoleById(Long id);
    GenericResponse createRole(CreateRoleRequest request);
    GenericResponse updateRole(Long id, CreateRoleRequest request);
    GenericResponse deleteRole(Long id);
    GenericResponse activateRole(Long id);
    GenericResponse deactivateRole(Long id);
    GenericResponse searchRoles(String search, Pageable pageable);
    @Transactional
    GenericResponse assignRolesToUser(AssignRoleRequest request);
    GenericResponse revokeRoleFromUser(Long userId, Long roleId);
    GenericResponse getUserRoles(Long userId);
    GenericResponse getRoleUsers(Long roleId, Pageable pageable);
}

