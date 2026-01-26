package com.rathaur.nexus.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@io.swagger.v3.oas.annotations.media.Schema(name = "ApiResponse")
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private ApiError error;
    private Instant timestamp;
    private String traceId;

    public static <T> ApiResponse<T> ok(String message, T data, String traceId) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        r.data = data;
        r.timestamp = Instant.now();
        r.traceId = traceId;
        return r;
    }

    public static <T> ApiResponse<T> fail(String message, ApiError error, String traceId) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        r.error = error;
        r.timestamp = Instant.now();
        r.traceId = traceId;
        return r;
    }
}