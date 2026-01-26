package com.rathaur.nexus.common.exception;

import com.rathaur.nexus.common.dto.ApiError;
import com.rathaur.nexus.common.dto.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseExceptionHandler {

    @Autowired(required = false)
    private Tracer tracer;

    /** * 1. PROTOCOL: Method Not Allowed (e.g. POST to a GET endpoint)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ApiError error = new ApiError("METHOD_NOT_ALLOWED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.fail("Protocol Error", error, getTraceId()));
    }

    /**
     * 2. SECURITY: JWT Infrastructure Exceptions
     * These are caught in the Filter and delegated here via HandlerExceptionResolver.
     */
    @ExceptionHandler({ExpiredJwtException.class, SignatureException.class, io.jsonwebtoken.MalformedJwtException.class})
    public ResponseEntity<ApiResponse<Void>> handleSecurityInfrastructure(Exception ex) {
        String code = "INVALID_TOKEN";
        String msg = "Security token is invalid or malformed.";

        if (ex instanceof ExpiredJwtException) {
            code = "TOKEN_EXPIRED";
            msg = "Your session has expired. Please log in again.";
        }

        ApiError error = new ApiError(code, msg);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("Authentication Failed", error, getTraceId()));
    }

    /** * 3. VALIDATION: @Valid failures
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiError.FieldError(f.getField(), f.getDefaultMessage()))
                .collect(Collectors.toList());

        ApiError error = new ApiError("VALIDATION_FAILED", "Input validation failed");
        error.setFieldErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("Validation Error", error, getTraceId()));
    }

    /** * 4. SAFETY NET: Catch-all
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        // Log the actual exception here for Loki/Zipkin
        ApiError error = new ApiError("INTERNAL_SERVER_ERROR", "An unexpected error occurred.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("Server Error", error, getTraceId()));
    }

    protected String getTraceId() {
        return (tracer != null && tracer.currentSpan() != null) ? tracer.currentSpan().context().traceId() : "N/A";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        ApiError error = new ApiError("ACCESS_DENIED", "You are not authorized to access this resource.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("Authorization Failed", error, getTraceId()));
    }

}