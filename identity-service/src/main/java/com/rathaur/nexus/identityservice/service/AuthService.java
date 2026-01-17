package com.rathaur.nexus.identityservice.service;

import com.rathaur.nexus.common.security.JwtUtils;
import com.rathaur.nexus.identityservice.entity.UserCredential;
import com.rathaur.nexus.identityservice.repository.UserCredentialRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/17/2026
 */
@Service
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


    public AuthService(UserCredentialRepository userCredentialRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils){
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils =jwtUtils;
    }

    public String saveUser(UserCredential credential){
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        userCredentialRepository.save(credential);
        return "User Added to the system";
    }

    public String generateToken(String username){
        return jwtUtils.generateToken(username);
    }
}
