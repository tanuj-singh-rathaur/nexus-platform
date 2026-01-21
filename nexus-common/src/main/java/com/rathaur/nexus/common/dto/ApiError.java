package com.rathaur.nexus.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
@Data
@AllArgsConstructor
@io.swagger.v3.oas.annotations.media.Schema(name = "ApiError")
public class ApiError {
    public String code;     // e.g. AUTH_001
    public String details;  // human readable
    public String path;
    public java.util.List<FieldError> fieldErrors;

    public ApiError() {}

    public ApiError(String code, String details) {
        this.code = code;
        this.details = details;
    }

    public static class FieldError {
        public String field;
        public String message;

        public FieldError(String field, @Nullable String defaultMessage) {
        }
    }
}
