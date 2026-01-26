package com.rathaur.nexus.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Standard error structure for all Nexus services.
 * @author Tanuj Singh Rathaur
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@io.swagger.v3.oas.annotations.media.Schema(name = "ApiError")
public class ApiError {
    private String code;     // e.g. AUTH_001
    private String details;  // Human readable message
    private String path;
    private List<FieldError> fieldErrors;

    public ApiError(String code, String details) {
        this.code = code;
        this.details = details;
    }

    @Data
    @NoArgsConstructor // Required for JSON deserialization
    @AllArgsConstructor // Fixes the "Default constructor invoked with arguments" error
    public static class FieldError {
        private String field;
        private String message;
    }
}