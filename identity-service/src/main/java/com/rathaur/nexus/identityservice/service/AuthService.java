package com.rathaur.nexus.identityservice.service;

import com.rathaur.nexus.common.config.RabbitMQConfig;
import com.rathaur.nexus.common.event.UserRegistrationEvent;
import com.rathaur.nexus.common.security.JwtUtils;
import com.rathaur.nexus.identityservice.dto.AuthRequest;
import com.rathaur.nexus.identityservice.entity.UserCredential;
import com.rathaur.nexus.identityservice.repository.UserCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RabbitTemplate rabbitTemplate;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserCredentialRepository userCredentialRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       RabbitTemplate rabbitTemplate) {
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.rabbitTemplate = rabbitTemplate;
    }

    public String saveUser(AuthRequest request) {
        // 1. Create Entity and Encode Password
        UserCredential credential = new UserCredential();
        credential.setName(request.getName());
        credential.setUsername(request.getUsername());
        credential.setEmail(request.getEmail());
        credential.setPassword(passwordEncoder.encode(request.getPassword()));

        userCredentialRepository.save(credential);

        // 2. Prepare Event for Portfolio Service (Clean data only)
        UserRegistrationEvent event = new UserRegistrationEvent(
                request.getUsername(),
                request.getEmail(),
                request.getName()
        );

        // 3. Publish to RabbitMQ
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event
        );

        log.info("✅ SUCCESS: User saved and registration event published for: {}", request.getUsername());
        return "User " + request.getUsername() + " registered successfully. Profile creation triggered.";
    }

    public String generateToken(String username) {
        return jwtUtils.generateToken(username);
    }
}