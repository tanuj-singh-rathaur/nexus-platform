package com.rathaur.nexus.apigateway.controller;

import com.rathaur.nexus.apigateway.dto.ApiError;
import com.rathaur.nexus.apigateway.dto.ApiResponse;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Gateway Fallback Controller using Nexus Standard ApiResponse.
 * Ensures the frontend receives a consistent JSON structure even during service outages.
 * * @author Tanuj Singh Rathaur
 */
@RestController
@RequestMapping("/fallback")
@RequiredArgsConstructor
public class FallbackController {

    private final Tracer tracer;

    @RequestMapping("/identity")
    public Mono<ResponseEntity<ApiResponse<Void>>> identityFallback() {
        ApiError error = new ApiError("SERVICE_UNAVAILABLE", "Authentication service is temporarily offline.");

        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail("Identity Service Offline", error, getTraceId())));
    }

    @RequestMapping("/portfolio")
    public Mono<ResponseEntity<ApiResponse<Void>>> portfolioFallback() {
        ApiError error = new ApiError("MAINTENANCE", "Portfolio data is currently unavailable.");

        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail("Portfolio Service Offline", error, getTraceId())));
    }

    @RequestMapping("/stats")
    public Mono<ResponseEntity<ApiResponse<Void>>> statsFallback() {
        ApiError error = new ApiError("MAINTENANCE", "Stats and Leaderboard service is temporarily offline.");

        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail("Stats Service Offline", error, getTraceId())));
    }

    /**
     * Captures the traceId from the Gateway context to link the
     * fallback event to the original request logs.
     */
    private String getTraceId() {
        return (tracer.currentSpan() != null)
                ? tracer.currentSpan().context().traceId()
                : "gw-" + java.util.UUID.randomUUID();
    }
}