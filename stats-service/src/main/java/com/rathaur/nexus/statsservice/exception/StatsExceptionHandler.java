package com.rathaur.nexus.statsservice.exception;

import com.rathaur.nexus.common.dto.ApiError;
import com.rathaur.nexus.common.dto.ApiResponse;
import com.rathaur.nexus.common.exception.BaseExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler for Stats Service.
 * @author Tanuj Singh Rathaur
 */
@RestControllerAdvice
public class StatsExceptionHandler extends BaseExceptionHandler {

    /**
     * Handle cases where a UserStats master record or specific stat entry is missing.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
        ApiError error = new ApiError("STATS_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("Resource Not Found", error, getTraceId()));
    }

    /**
     * Handle business logic failures during the sync process.
     */
    @ExceptionHandler(StatsDomainExceptions.SyncServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleSyncError(StatsDomainExceptions.SyncServiceException ex) {
        ApiError error = new ApiError("SYNC_FAILURE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("Background Synchronization Failed", error, getTraceId()));
    }

    /**
     * Handle failures when parsing external API responses (GitHub/LeetCode).
     */
    @ExceptionHandler(StatsDomainExceptions.DataParsingException.class)
    public ResponseEntity<ApiResponse<Void>> handleParsingError(StatsDomainExceptions.DataParsingException ex) {
        ApiError error = new ApiError("STATS_PARSE_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.fail("External Data structure is invalid", error, getTraceId()));
    }

    @ExceptionHandler(StatsDomainExceptions.ExternalProviderThrottledException.class)
    public ResponseEntity<ApiResponse<Void>> handleThrottling(StatsDomainExceptions.ExternalProviderThrottledException ex) {
        ApiError error = new ApiError("PROVIDER_THROTTLED", "Nexus is being throttled by external providers. Try again in 1 hour.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.fail("External API Limit Reached", error, getTraceId()));
    }

}