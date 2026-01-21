package com.rathaur.nexus.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't send null fields to the frontend
@io.swagger.v3.oas.annotations.media.Schema(name = "ApiResponse")
public class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;
    public ApiError error;
    public java.time.Instant timestamp;
    public String traceId;

    public static <T> ApiResponse<T> ok(String message, T data, String traceId) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        r.data = data;
        r.timestamp = java.time.Instant.now();
        r.traceId = traceId;
        return r;
    }

    public static <T> ApiResponse<T> fail(String message, ApiError error, String traceId) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        r.error = error;
        r.timestamp = java.time.Instant.now();
        r.traceId = traceId;
        return r;
    }
}