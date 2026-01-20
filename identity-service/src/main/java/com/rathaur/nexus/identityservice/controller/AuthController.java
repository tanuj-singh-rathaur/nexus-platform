package com.rathaur.nexus.identityservice.controller;

import com.rathaur.nexus.identityservice.dto.AuthRequest;
import com.rathaur.nexus.identityservice.service.AuthService;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Observed(name = "identity.controller")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService, AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public String registerUser(@RequestBody @Valid AuthRequest authRequest) {
        log.info("📢 REGISTER: Creating account for username: {}", authRequest.getUsername());
        return authService.saveUser(authRequest);
    }

    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequest authRequest) {
        log.info("📢 LOGIN: Authenticating user: {}", authRequest.getUsername());

        // This will now match against the 'username' field in your DB
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        if (authentication.isAuthenticated()) {
            return authService.generateToken(authRequest.getUsername());
        } else {
            log.error("❌ AUTH FAILED: Invalid credentials for {}", authRequest.getUsername());
            throw new RuntimeException("Invalid access: Authentication failed");
        }
    }
}