package com.rathaur.nexus.identityservice.controller;

import com.rathaur.nexus.common.dto.ApiResponse;
import com.rathaur.nexus.identityservice.dto.AuthRequest;
import com.rathaur.nexus.identityservice.dto.AuthTokenResponse;
import com.rathaur.nexus.identityservice.service.AuthService;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.Tracer;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Authentication Controller for Nexus Identity Service.
 * Provides endpoints for user registration, login (token generation), and validation.
 * * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
@RestController
@RequestMapping("/auth")
@Observed(name = "identity.controller")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final Tracer tracer;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService, AuthenticationManager authenticationManager, Tracer tracer) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.tracer = tracer;
    }

    /**
     * Registers a new user and triggers an outbox event for profile creation.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerUser(@RequestBody @Valid AuthRequest authRequest) {
        log.info("AUTH-FLOW: Initiating registration for user: {}", authRequest.getUsername());

        String result = authService.saveUser(authRequest);
        String traceId = getTraceId();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("User registered successfully", result, traceId));
    }

    /**
     * Authenticates user credentials and returns Access/Refresh tokens.
     */
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> getToken(@RequestBody AuthRequest authRequest) {
        log.info("AUTH-FLOW: Login attempt for user: {}", authRequest.getUsername());

        try {
            /* Standard Spring Security Auth check */
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            if (authentication.isAuthenticated()) {
                /* Map authorities to strings for JWT claims */
                List<String> roles = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList();

                /* Generate the SaaS token pair */
                String accessToken = authService.generateAccessToken(authRequest.getUsername(), roles);
                String refreshToken = authService.generateRefreshToken(authRequest.getUsername());

                AuthTokenResponse resp = new AuthTokenResponse(
                        "Bearer",
                        accessToken,
                        refreshToken,
                        600, /* 10 Minutes matches AuthService constant */
                        authRequest.getUsername(),
                        roles
                );

                return ResponseEntity.ok(ApiResponse.ok("Login successful", resp, getTraceId()));
            } else {
                /* This is a fallback, authenticate() usually throws BadCredentialsException */
                throw new BadCredentialsException("Authentication failed");
            }
        } catch (BadCredentialsException e) {
            log.error("AUTH-ERROR: Invalid credentials for user: {}", authRequest.getUsername());
            /* Re-throwing allows IdentityExceptionHandler to catch and format the response */
            throw e;
        }
    }

    /**
     * Validates an existing token.
     * Essential for the API Gateway to verify access before routing to internal services.
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestParam("token") String token) {
        log.info("AUTH-FLOW: Validating token integrity");

        authService.validateToken(token);

        return ResponseEntity.ok(ApiResponse.ok("Token is valid", "Success", getTraceId()));
    }

    /**
     * Helper to extract current trace ID for unified observability.
     */
    private String getTraceId() {
        return (tracer.currentSpan() != null)
                ? tracer.currentSpan().context().traceId()
                : "N/A";
    }
}