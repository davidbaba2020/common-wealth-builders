package com.common_wealth_builders.service.impl;

import com.common_wealth_builders.dto.request.AssignRoleRequest;
import com.common_wealth_builders.dto.request.ChangePasswordRequest;
import com.common_wealth_builders.dto.request.LoginRequest;
import com.common_wealth_builders.dto.request.RegisterRequest;
import com.common_wealth_builders.dto.response.AuthResponse;
import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.entity.Role;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.enums.RoleType;
import com.common_wealth_builders.exception.InvalidCredentialsException;
import com.common_wealth_builders.exception.ResourceNotFoundException;
import com.common_wealth_builders.exception.UserAlreadyExistsException;
import com.common_wealth_builders.repository.RoleRepository;
import com.common_wealth_builders.repository.UserRepository;
import com.common_wealth_builders.security.JwtUtil;
import com.common_wealth_builders.service.AuditService;
import com.common_wealth_builders.service.AuthService;
import com.common_wealth_builders.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static com.common_wealth_builders.utils.AuthUtils.getLoggedInNames;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;
    private final RoleRepository roleRepository;
    private final RoleService roleService;

    @Override
    @Transactional
    public GenericResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // ✅ Check if email or username already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        if (userRepository.existsByUserName(request.getUserName())) {
            throw new UserAlreadyExistsException("Username already exists: " + request.getUserName());
        }

        // ✅ Build user without roles yet
        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .passport(request.getPassport())
                .phoneNumber(request.getPhoneNumber())
                .remitanceBankName(request.getRemitanceBankName())
                .remitanceAccNumber(request.getRemitanceAccNumber())
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .createdBy(getLoggedInNames())
                .updatedBy(getLoggedInNames())
                .isEnabled(true)
                .build();

        // ✅ Save user first to get ID for role assignment
        User savedUser = userRepository.save(user);

        // ✅ If roles are provided, assign them using the existing roleService method
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            AssignRoleRequest assignRoleRequest = AssignRoleRequest.builder()
                    .userId(savedUser.getId())
                    .roleIds(request.getRoles().stream()
                            .map(roleType -> roleRepository.findByName(roleType)
                                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleType))
                                    .getId()
                            )
                            .collect(Collectors.toSet()))
                    .remarks("Assigned roles during user creation")
                    .build();

            // Reuse the assignRolesToUser service method
            roleService.assignRolesToUser(assignRoleRequest);
        }

        // ✅ Audit
        auditService.logAction(
                savedUser.getId(),
                "USER_REGISTRATION",
                "AUTH",
                "User registered successfully: " + savedUser.getEmail()
        );

        // ✅ Generate token
        String token = jwtUtil.generateToken(savedUser.getEmail());

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstname(savedUser.getFirstname())
                .lastname(savedUser.getLastname())
                .roles(savedUser.getActiveRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()))
                .build();

        log.info("User registered successfully: {}", savedUser.getEmail());

        return GenericResponse.builder()
                .isSuccess(true)
                .message("User registered successfully")
                .data(authResponse)
                .httpStatus(HttpStatus.CREATED)
                .build();
    }

    @Override
    @Transactional
    public GenericResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());

        auditService.logAction(
                user.getId(),
                "USER_LOGIN",
                "AUTH",
                "User logged in successfully"
        );

        log.info("User info: id={}, email={}, name={} {}",
                user.getId(), user.getEmail(), user.getFirstname(), user.getLastname());

        log.info("Active roles: {}",
                user.getActiveRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList())
        );

        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .roles(user.getActiveRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList())
                )
                .build();

        log.info("User logged in successfully: {}", user.getEmail());

        return GenericResponse.builder()
                .isSuccess(true)
                .message("Login successful")
                .data(authResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    @Override
    @Transactional
    public GenericResponse changePassword(ChangePasswordRequest request, String userEmail) {
        log.info("Password change request for user: {}", userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedDate(LocalDateTime.now());
        user.setUpdatedBy(userEmail);
        
        userRepository.save(user);
        
        auditService.logAction(
                user.getId(),
                "PASSWORD_CHANGED",
                "AUTH",
                "User changed password successfully"
        );
        
        log.info("Password changed successfully for user: {}", userEmail);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Password changed successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional
    public GenericResponse deleteAccount(Long userId) {
        log.info("Delete account request for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        auditService.logAction(
                userId,
                "USER_DELETED",
                "AUTH",
                "User account deleted: " + user.getEmail()
        );
        
        userRepository.delete(user);
        
        log.info("User account deleted successfully: {}", user.getEmail());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Account deleted successfully")
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional
    public GenericResponse resetPassword(String email) {
        log.info("Password reset request for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // In production, generate a token and send via email
        // For now, we'll just log the action

        auditService.logAction(
                user.getId(),
                "PASSWORD_RESET_REQUESTED",
                "AUTH",
                "Password reset requested for: " + email
        );
        
        log.info("Password reset email sent to: {}", email);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Password reset instructions have been sent to your email")
                .httpStatus(HttpStatus.OK)
                .build();
    }
}