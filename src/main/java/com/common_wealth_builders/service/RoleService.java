package com.common_wealth_builders.service;

import com.common_wealth_builders.dto.request.AssignRoleRequest;
import com.common_wealth_builders.dto.request.RoleRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import org.springframework.data.domain.Pageable;

public interface RoleService {
    GenericResponse getAllRoles(Pageable pageable);
    GenericResponse getActiveRoles();
    GenericResponse getRoleById(Long id);
    GenericResponse createRole(RoleRequest request);
    GenericResponse updateRole(Long id, RoleRequest request);
    GenericResponse deleteRole(Long id);
    GenericResponse activateRole(Long id);
    GenericResponse deactivateRole(Long id);
    GenericResponse searchRoles(String search, Pageable pageable);
    GenericResponse assignRoleToUser(AssignRoleRequest request);
    GenericResponse revokeRoleFromUser(Long userId, Long roleId);
    GenericResponse getUserRoles(Long userId);
    GenericResponse getRoleUsers(Long roleId, Pageable pageable);
}

