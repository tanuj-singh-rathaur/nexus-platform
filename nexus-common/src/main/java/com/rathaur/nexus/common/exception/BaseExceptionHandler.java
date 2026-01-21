package com.rathaur.nexus.common.exception;

import com.rathaur.nexus.common.dto.ApiError;
import com.rathaur.nexus.common.dto.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Common Base for all Exception Handlers.
 * Microservices will extend this class to inherit standard error formatting.
 */
public abstract class BaseExceptionHandler {

    /* Handle JWT Expiration specifically for SaaS UX */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwt(ExpiredJwtException ex) {
        ApiError error = new ApiError("TOKEN_EXPIRED", "Your session has expired. Please login again.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Unauthorized", error, null));
    }

    /* Handle Invalid Signatures (Tampering) */
    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidSignature(SignatureException ex) {
        ApiError error = new ApiError("INVALID_TOKEN", "The security token is invalid or tampered with.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("Unauthorized", error, null));
    }

    /* Handle @Valid Validation Failures */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiError.FieldError(f.getField(), f.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiError error = new ApiError("VALIDATION_FAILED", "Input validation failed");
        error.setFieldErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("Validation Error", error, null));
    }

    /* Fallback for all other Runtime Exceptions */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(RuntimeException ex) {
        ApiError error = new ApiError("INTERNAL_SERVER_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Error", error, null));
    }
}