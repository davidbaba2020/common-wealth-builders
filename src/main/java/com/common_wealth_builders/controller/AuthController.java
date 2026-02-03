package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.ChangePasswordRequest;
import com.common_wealth_builders.dto.request.LoginRequest;
import com.common_wealth_builders.dto.request.RegisterRequest;
import com.common_wealth_builders.dto.request.ResetPasswordRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<GenericResponse> register(@Valid @RequestBody RegisterRequest request) {
        GenericResponse response = authService.register(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PostMapping("/login")
    public ResponseEntity<GenericResponse> login(@Valid @RequestBody LoginRequest request) {
        GenericResponse response = authService.login(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        GenericResponse response = authService.changePassword(request, userEmail);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<GenericResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        GenericResponse response = authService.resetPassword(request.getEmail());
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> deleteUser(@PathVariable Long userId) {
        GenericResponse response = authService.deleteAccount(userId);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}