package com.common_wealth_builders.service;


import com.common_wealth_builders.dto.request.ChangePasswordRequest;
import com.common_wealth_builders.dto.request.LoginRequest;
import com.common_wealth_builders.dto.request.RegisterRequest;
import com.common_wealth_builders.dto.response.GenericResponse;

public interface AuthService {
    GenericResponse register(RegisterRequest request);
    GenericResponse login(LoginRequest request);
    GenericResponse changePassword(ChangePasswordRequest request, String userEmail);
    GenericResponse deleteAccount(Long userId);
    GenericResponse resetPassword(String email);
}