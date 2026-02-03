package com.common_wealth_builders.exception;

import com.common_wealth_builders.dto.response.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        
        List<String> errorMessages = new ArrayList<>();
        
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            errorMessages.add(error.getDefaultMessage());
        });
        
        log.error("Validation failed: {}", errorMessages);
        
        return ResponseEntity
                .badRequest()
                .body(GenericResponse.builder()
                        .isSuccess(false)
                        .message("Validation Failed: Please provide valid data.")
                        .data(errorMessages)
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build());
    }
    
    @ExceptionHandler(value = UserAlreadyExistsException.class)
    public ResponseEntity<GenericResponse> handleUserAlreadyExistsException(
            UserAlreadyExistsException exception) {
        
        log.error("User already exists: {}", exception.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(GenericResponse.builder()
                        .isSuccess(false)
                        .message(exception.getMessage())
                        .httpStatus(HttpStatus.CONFLICT)
                        .build());
    }
    
    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<GenericResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception) {
        
        log.error("Resource not found: {}", exception.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(GenericResponse.builder()
                        .isSuccess(false)
                        .message(exception.getMessage())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());
    }
    
    @ExceptionHandler(value = InvalidCredentialsException.class)
    public ResponseEntity<GenericResponse> handleInvalidCredentialsException(
            InvalidCredentialsException exception) {
        
        log.error("Invalid credentials: {}", exception.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .isSuccess(false)
                        .message(exception.getMessage())
                        .httpStatus(HttpStatus.UNAUTHORIZED)
                        .build());
    }
    
    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<GenericResponse> handleBadCredentialsException(
            BadCredentialsException exception) {
        
        log.error("Bad credentials: {}", exception.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .isSuccess(false)
                        .message("Invalid email or password")
                        .httpStatus(HttpStatus.UNAUTHORIZED)
                        .build());
    }
    
    @ExceptionHandler(value = UnauthorizedException.class)
    public ResponseEntity<GenericResponse> handleUnauthorizedException(
            UnauthorizedException exception) {
        
        log.error("Unauthorized: {}", exception.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponse.builder()
                        .isSuccess(false)
                        .message(exception.getMessage())
                        .httpStatus(HttpStatus.UNAUTHORIZED)
                        .build());
    }
    
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<GenericResponse> handleAccessDeniedException(
            AccessDeniedException exception) {
        
        log.error("Access denied: {}", exception.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(GenericResponse.builder()
                        .isSuccess(false)
                        .message("You do not have permission to perform this action")
                        .httpStatus(HttpStatus.FORBIDDEN)
                        .build());
    }
    
    @ExceptionHandler(value = PaymentAlreadyVerifiedException.class)
    public ResponseEntity<GenericResponse> handlePaymentAlreadyVerifiedException(
            PaymentAlreadyVerifiedException exception) {
        
        log.error("Payment already verified: {}", exception.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(GenericResponse.builder()
                        .isSuccess(false)
                        .message(exception.getMessage())
                        .httpStatus(HttpStatus.CONFLICT)
                        .build());
    }
    
    @ExceptionHandler(value = InsufficientPermissionException.class)
    public ResponseEntity<GenericResponse> handleInsufficientPermissionException(
            InsufficientPermissionException exception) {
        
        log.error("Insufficient permission: {}", exception.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(GenericResponse.builder()
                        .isSuccess(false)
                        .message(exception.getMessage())
                        .httpStatus(HttpStatus.FORBIDDEN)
                        .build());
    }
    
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<GenericResponse> handleGenericException(Exception exception) {
        
        log.error("An error occurred: {}", exception.getMessage(), exception);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericResponse.builder()
                        .isSuccess(false)
                        .message("An internal server error occurred. Please try again later.")
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }

    @ExceptionHandler(value = RoleAlreadyExistsException.class)
    public ResponseEntity<GenericResponse> handleRoleAlreadyExistsException(
            RoleAlreadyExistsException exception) {

        log.error("Role already exists: {}", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(GenericResponse.builder()
                        .isSuccess(false)
                        .message(exception.getMessage())
                        .httpStatus(HttpStatus.CONFLICT)
                        .build());
    }

}