package com.common_wealth_builders.service.impl;

import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.dto.response.PageResponse;
import com.common_wealth_builders.entity.User;
import com.common_wealth_builders.exception.ResourceNotFoundException;
import com.common_wealth_builders.repository.UserRepository;
import com.common_wealth_builders.service.AuditService;
import com.common_wealth_builders.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final AuditService auditService;
    
    @Override
    public GenericResponse getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination");
        
        Page<User> usersPage = userRepository.findAll(pageable);
        
        List<Map<String, Object>> userResponses = usersPage.getContent().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        
        PageResponse<Map<String, Object>> pageResponse = PageResponse.<Map<String, Object>>builder()
                .content(userResponses)
                .pageNumber(usersPage.getNumber())
                .pageSize(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .last(usersPage.isLast())
                .first(usersPage.isFirst())
                .build();
        
        log.info("Successfully fetched {} users", userResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Users retrieved successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        log.info("User retrieved successfully: id={}", id);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("User retrieved successfully")
                .data(mapToUserResponse(user))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse searchUsers(String search, Pageable pageable) {
        log.info("Searching users with term: {}", search);
        
        Page<User> usersPage = userRepository.searchUsers(search, pageable);
        
        List<Map<String, Object>> userResponses = usersPage.getContent().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
        
        PageResponse<Map<String, Object>> pageResponse = PageResponse.<Map<String, Object>>builder()
                .content(userResponses)
                .pageNumber(usersPage.getNumber())
                .pageSize(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .last(usersPage.isLast())
                .first(usersPage.isFirst())
                .build();
        
        log.info("Search completed: found {} users", userResponses.size());
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("Search completed successfully")
                .data(pageResponse)
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional
    public GenericResponse enableUser(Long id) {
        log.info("Enabling user: id={}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        user.setEnabled(true);
        userRepository.save(user);
        
        auditService.logAction(
                user.getId(),
                "USER_ENABLED",
                "USERS",
                "User account enabled: " + user.getEmail()
        );
        
        log.info("User enabled successfully: id={}", id);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("User enabled successfully")
                .data(mapToUserResponse(user))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    @Transactional
    public GenericResponse disableUser(Long id) {
        log.info("Disabling user: id={}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        user.setEnabled(false);
        userRepository.save(user);
        
        auditService.logAction(
                user.getId(),
                "USER_DISABLED",
                "USERS",
                "User account disabled: " + user.getEmail()
        );
        
        log.info("User disabled successfully: id={}", id);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("User disabled successfully")
                .data(mapToUserResponse(user))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    @Override
    public GenericResponse getUserProfile(String email) {
        log.info("Fetching user profile for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        log.info("User profile retrieved successfully: email={}", email);
        
        return GenericResponse.builder()
                .isSuccess(true)
                .message("User profile retrieved successfully")
                .data(mapToUserResponse(user))
                .httpStatus(HttpStatus.OK)
                .build();
    }
    
    private Map<String, Object> mapToUserResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("firstname", user.getFirstname());
        response.put("lastname", user.getLastname());
        response.put("email", user.getEmail());
        response.put("userName", user.getUsername());
        response.put("phoneNumber", user.getPhoneNumber());
        response.put("passport", user.getPassport());
        response.put("remitanceBankName", user.getRemitanceBankName());
        response.put("remitanceAccNumber", user.getRemitanceAccNumber());
        response.put("isEnabled", user.isEnabled());
        response.put("isAccountNonExpired", user.isAccountNonExpired());
        response.put("isAccountNonLocked", user.isAccountNonLocked());
        response.put("isCredentialsNonExpired", user.isCredentialsNonExpired());
        response.put("lastLoginDate", user.getLastLoginDate());
        response.put("lastLoginIp", user.getLastLoginIp());
        response.put("createdDate", user.getCreatedDate());
        response.put("updatedDate", user.getUpdatedDate());
        
        List<String> roles = user.getUserRoles().stream()
                .filter(ur -> ur.isActive())
                .map(ur -> ur.getRole().getName().name())
                .collect(Collectors.toList());
        response.put("roles", roles);
        
        return response;
    }
}