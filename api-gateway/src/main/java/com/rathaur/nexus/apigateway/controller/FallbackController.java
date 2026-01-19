package com.rathaur.nexus.apigateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/19/2026
 */
@RequestMapping("/fallback")
@RestController
public class FallbackController {

    @RequestMapping("/identity")
    public Mono<Map<String, String>> identityFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Authentication service is temporarily unavailable. Please try again later.");
        response.put("status", "SERVICE_UNAVAILABLE");
        return Mono.just(response);
    }

    @RequestMapping("/portfolio")
    public Mono<Map<String, String>> portfolioFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Portfolio data is currently offline. We are working to restore it.");
        response.put("status", "MAINTENANCE");
        return Mono.just(response);
    }
}
