package com.rathaur.nexus.identityservice.controller;

import com.rathaur.nexus.identityservice.dto.AuthRequest;
import com.rathaur.nexus.identityservice.entity.UserCredential;
import com.rathaur.nexus.identityservice.service.AuthService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/17/2026
 */

@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthService authService;
    private AuthenticationManager authenticationManager;
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService, AuthenticationManager authenticationManager){
        this.authService =authService;
        this.authenticationManager = authenticationManager;
    }


    @PostMapping("/register")
    public String registerUser(@RequestBody @Valid UserCredential userCredential){
        log.info("📢 TRACE CHECK: Registering user {}", userCredential.getName());
        return authService.saveUser(userCredential);
    }

    @PostMapping("/token")
    public String getToken(@RequestBody AuthRequest authRequest){
        log.info("📢 TRACE CHECK: Registering user {}", authRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if(authentication.isAuthenticated())
            return authService.generateToken(authRequest.getUsername());
        else
            throw new RuntimeException("invalid access");

    }


}
