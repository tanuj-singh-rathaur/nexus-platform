package com.rathaur.nexus.identityservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rathaur.nexus.common.event.UserRegistrationEvent;
import com.rathaur.nexus.common.security.JwtUtils;
import com.rathaur.nexus.identityservice.dto.AuthRequest;
import com.rathaur.nexus.identityservice.entity.OutboxMessage;
import com.rathaur.nexus.identityservice.entity.UserCredential;
import com.rathaur.nexus.identityservice.repository.OutboxRepository;
import com.rathaur.nexus.identityservice.repository.UserCredentialRepository;
import io.micrometer.tracing.Tracer;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final OutboxRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final ObjectMapper objectMapper;
    private final Tracer tracer;

    public AuthService(UserCredentialRepository userCredentialRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       ObjectMapper objectMapper,
                       OutboxRepository outboxRepository,
                       Tracer tracer) {
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
        this.tracer = tracer;
    }

    @Transactional
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

        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setAggregateId(request.getUsername());
        outboxMessage.setPayload(serializeToJson(event));

        if(Objects.nonNull(tracer.currentSpan())){
            outboxMessage.setSpanId(tracer.currentSpan().context().spanId());
            outboxMessage.setTraceId(tracer.currentSpan().context().traceId());
        }

        outboxRepository.save(outboxMessage);

        log.info("✅ SUCCESS: User saved and registration event published for: {}", request.getUsername());
        return "User " + request.getUsername() + " registered successfully. Profile creation triggered.";
    }

    public String generateToken(String username) {
        return jwtUtils.generateToken(username);
    }

    private String serializeToJson(UserRegistrationEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert event to JSON", e);
        }
    }

}