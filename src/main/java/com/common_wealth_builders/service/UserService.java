package com.common_wealth_builders.service;

import com.common_wealth_builders.dto.response.GenericResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    GenericResponse getAllUsers(Pageable pageable);
    GenericResponse getUserById(Long id);
    GenericResponse searchUsers(String search, Pageable pageable);
    GenericResponse enableUser(Long id);
    GenericResponse disableUser(Long id);
    GenericResponse getUserProfile(String email);
}