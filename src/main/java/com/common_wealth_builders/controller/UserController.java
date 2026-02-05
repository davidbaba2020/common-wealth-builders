package com.common_wealth_builders.controller;

import com.common_wealth_builders.dto.response.GenericResponse;
import com.common_wealth_builders.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder) {
        
        log.info("Request received: GET /users - page={}, size={}, sortBy={}, sortOrder={}", 
                page, size, sortBy, sortOrder);
        
        Sort.Direction direction = sortOrder.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        GenericResponse response = userService.getAllUsers(pageable);
        
        log.info("Response sent: GET /users - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> getUserById(@PathVariable Long id) {
        log.info("Request received: GET /users/{} - id={}", id, id);
        
        GenericResponse response = userService.getUserById(id);
        
        log.info("Response sent: GET /users/{} - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GenericResponse> getUserProfile(Authentication authentication) {
        log.info("Request received: GET /users/profile - email={}", authentication.getName());
        
        GenericResponse response = userService.getUserProfile(authentication.getName());
        
        log.info("Response sent: GET /users/profile - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> searchUsers(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Request received: GET /users/search - search={}, page={}, size={}", 
                search, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        GenericResponse response = userService.searchUsers(search, pageable);
        
        log.info("Response sent: GET /users/search - status={}, success={}", 
                response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> enableUser(@PathVariable Long id) {
        log.info("Request received: POST /users/{}/enable - id={}", id, id);
        
        GenericResponse response = userService.enableUser(id);
        
        log.info("Response sent: POST /users/{}/enable - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
    
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN', 'ROLE_TECH_ADMIN')")
    public ResponseEntity<GenericResponse> disableUser(@PathVariable Long id) {
        log.info("Request received: POST /users/{}/disable - id={}", id, id);
        
        GenericResponse response = userService.disableUser(id);
        
        log.info("Response sent: POST /users/{}/disable - status={}, success={}", 
                id, response.getHttpStatus(), response.isSuccess());
        
        return new ResponseEntity<>(response, response.getHttpStatus());
    }
}