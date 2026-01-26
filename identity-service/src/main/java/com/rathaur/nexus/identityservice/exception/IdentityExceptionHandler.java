package com.rathaur.nexus.identityservice.exception;

import com.rathaur.nexus.common.dto.ApiError;
import com.rathaur.nexus.common.dto.ApiResponse;
import com.rathaur.nexus.common.exception.BaseExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.TimeoutException;

/**
 * Single Point of Truth for Identity Service Exceptions.
 * Inherits Global Infrastructure handlers (JWT, Validation) from BaseExceptionHandler.
 * * @author Tanuj Singh Rathaur
 */
@RestControllerAdvice
public class IdentityExceptionHandler extends BaseExceptionHandler {

    // --- 1. AUTHENTICATION & SECURITY ---

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        ApiError error = new ApiError("AUTH_INVALID_CREDENTIALS", "Invalid username or password.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("Login Failed", error, getTraceId()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        ApiError error = new ApiError("ACCESS_DENIED", "You do not have permission to access this resource.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("Access Denied", error, getTraceId()));
    }

    /**
     * Handles both Spring Security's LockedException and our Domain AccountLockedException
     */
    @ExceptionHandler({LockedException.class, IdentityDomainExceptions.AccountLockedException.class})
    public ResponseEntity<ApiResponse<Void>> handleLockedAccount(Exception ex) {
        ApiError error = new ApiError("AUTH_ACCOUNT_LOCKED", "Account locked due to too many failed attempts.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("Security Restriction", error, getTraceId()));
    }

    /**
     * Handles both Spring Security's DisabledException and our Domain AccountDisabledException
     */
    @ExceptionHandler({DisabledException.class, IdentityDomainExceptions.AccountDisabledException.class})
    public ResponseEntity<ApiResponse<Void>> handleDisabledAccount(Exception ex) {
        ApiError error = new ApiError("AUTH_ACCOUNT_DISABLED", "Account is disabled. Please verify your email.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("Account Inactive", error, getTraceId()));
    }

    @ExceptionHandler(IdentityDomainExceptions.InvalidRefreshTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRefreshToken(IdentityDomainExceptions.InvalidRefreshTokenException ex) {
        ApiError error = new ApiError("AUTH_INVALID_REFRESH_TOKEN", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("Session Error", error, getTraceId()));
    }

    // --- 2. REGISTRATION & DATA CONFLICTS ---

    @ExceptionHandler({IdentityDomainExceptions.UserAlreadyExistsException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiResponse<Void>> handleUserExists(Exception ex) {
        ApiError error = new ApiError("AUTH_USER_EXISTS", "User already exists! Please check your username or email.");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("Registration Conflict", error, getTraceId()));
    }

    // --- 3. RESOURCE & SYSTEM ERRORS ---

    @ExceptionHandler(IdentityDomainExceptions.ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(IdentityDomainExceptions.ResourceNotFoundException ex) {
        ApiError error = new ApiError("RESOURCE_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("Resource Not Found", error, getTraceId()));
    }

    @ExceptionHandler(IdentityDomainExceptions.RoleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleNotFound(IdentityDomainExceptions.RoleNotFoundException ex) {
        ApiError error = new ApiError("AUTH_CONFIG_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("System Error", error, getTraceId()));
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiResponse<Void>> handleTimeout(TimeoutException ex) {
        ApiError error = new ApiError("TIMEOUT", "The operation timed out. Please try again later.");
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(ApiResponse.fail("Timeout", error, getTraceId()));
    }
}