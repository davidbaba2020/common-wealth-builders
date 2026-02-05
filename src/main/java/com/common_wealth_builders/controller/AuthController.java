package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.request.ChangePasswordRequest;
import com.common_wealth_builders.dto.request.LoginRequest;
import com.common_wealth_builders.dto.request.RegisterRequest;
import com.common_wealth_builders.dto.request.ResetPasswordRequest;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.AuthService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and account management endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with the provided details. Returns JWT token upon successful registration."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data or email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<GenericResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterRequest.class))
            )
            @Valid @RequestBody RegisterRequest request) {
        GenericResponse response = authService.register(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns a JWT token for subsequent API calls"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account disabled or locked")
    })
    @PostMapping("/login")
    public ResponseEntity<GenericResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
            @Valid @RequestBody LoginRequest request) {
        GenericResponse response = authService.login(request);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Change password",
            description = "Allows authenticated users to change their password. Requires current password for verification."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid current password or password requirements not met"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericResponse> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Password change request with current and new passwords",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChangePasswordRequest.class))
            )
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        GenericResponse response = authService.changePassword(request, userEmail);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Request password reset",
            description = "Initiates password reset process by sending a reset link to the user's email address"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent successfully"),
            @ApiResponse(responseCode = "404", description = "Email address not found"),
            @ApiResponse(responseCode = "429", description = "Too many reset requests - please wait")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<GenericResponse> resetPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email address for password reset",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ResetPasswordRequest.class))
            )
            @Valid @RequestBody ResetPasswordRequest request) {
        GenericResponse response = authService.resetPassword(request.getEmail());
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @Operation(
            summary = "Delete user account",
            description = "Permanently deletes a user account. Only accessible by SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<GenericResponse> deleteUser(
            @Parameter(description = "ID of the user to delete", example = "1")
            @PathVariable Long userId) {
        GenericResponse response = authService.deleteAccount(userId);
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}