package com.rathaur.nexus.identityservice.exception;

import com.rathaur.nexus.common.dto.ApiError;
import com.rathaur.nexus.common.dto.ApiResponse;
import com.rathaur.nexus.common.exception.BaseExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class IdentityExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        ApiError error = new ApiError("AUTH_INVALID_CREDENTIALS", "Invalid username or password.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Login Failed", error, null));
    }

    @ExceptionHandler(UserAlreadyExistsException.class) // You need to create this custom exception class
    public ResponseEntity<ApiResponse<Void>> handleUserExists(UserAlreadyExistsException ex) {
        ApiError error = new ApiError("AUTH_USER_EXISTS", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.fail("Conflict", error, null));
    }
}