package com.rathaur.nexus.identityservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rathaur.nexus.common.event.UserRegistrationEvent;
import com.rathaur.nexus.common.security.JwtUtils;
import com.rathaur.nexus.common.utils.SecurityConstants;
import com.rathaur.nexus.identityservice.dto.AuthRequest;
import com.rathaur.nexus.identityservice.entity.OutboxMessage;
import com.rathaur.nexus.identityservice.entity.Role;
import com.rathaur.nexus.identityservice.entity.UserCredential;
import com.rathaur.nexus.identityservice.exception.UserAlreadyExistsException;
import com.rathaur.nexus.identityservice.repository.OutboxRepository;
import com.rathaur.nexus.identityservice.repository.UserCredentialRepository;
import io.micrometer.tracing.Tracer;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service handling Identity and Access Management (IAM) logic.
 * Implements the Transactional Outbox pattern for reliable event delivery.
 * * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final long ACCESS_EXPIRY_MS = 10 * 60 * 1000L;
    private static final long REFRESH_EXPIRY_MS = 15L * 24 * 60 * 60 * 1000L;

    private final UserCredentialRepository userCredentialRepository;
    private final OutboxRepository outboxRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
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
        validateUserUniqueness(request.getUsername(), request.getEmail());

        UserCredential credential = mapToEntity(request);
        userCredentialRepository.save(credential);

        String currentTraceId = (tracer.currentSpan() != null)
                ? tracer.currentSpan().context().traceId()
                : "internal-" + java.util.UUID.randomUUID();

        UserRegistrationEvent event = UserRegistrationEvent.create(
                credential.getUsername(),
                credential.getEmail(),
                credential.getName(),
                credential.getRole().name(),
                currentTraceId
        );

        // Atomic operation: user and outbox message committed together
        persistOutboxMessage(credential.getUsername(), event);

        log.info("IDENTITY-SERVICE: Successfully registered user and saved outbox event: {}", credential.getUsername());
        return "User " + credential.getUsername() + " registered successfully.";
    }

    @Transactional
    public void reverseUserRegistration(String username, String reason) {
        log.warn("SAGA-COMPENSATION: Reversing registration for user: {}. Reason: {}", username, reason);

        userCredentialRepository.findByUsername(username).ifPresentOrElse(
                user -> {
                    userCredentialRepository.delete(user);
                    log.info("SAGA-COMPENSATION: Successfully removed credentials for user: {}", username);
                },
                () -> log.error("SAGA-COMPENSATION-ERROR: User {} not found for reversal", username)
        );
    }

    public void validateToken(String token) {
        jwtUtils.parseAndValidate(token);
    }

    public String generateAccessToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_ACCESS);
        claims.put(SecurityConstants.CLAIM_ROLES, roles);
        return jwtUtils.generateToken(claims, username, ACCESS_EXPIRY_MS);
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_REFRESH);
        return jwtUtils.generateToken(claims, username, REFRESH_EXPIRY_MS);
    }

    private void validateUserUniqueness(String username, String email) {
        if (userCredentialRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username '" + username + "' is already taken.");
        }
        if (userCredentialRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email '" + email + "' is already registered.");
        }
    }

    private UserCredential mapToEntity(AuthRequest request) {
        UserCredential credential = new UserCredential();
        credential.setName(request.getName());
        credential.setUsername(request.getUsername());
        credential.setEmail(request.getEmail());
        credential.setPassword(passwordEncoder.encode(request.getPassword()));
        credential.setRole(Role.ROLE_USER);
        return credential;
    }

    private void persistOutboxMessage(String aggregateId, UserRegistrationEvent event) {
        OutboxMessage outboxMessage = OutboxMessage.builder()
                .aggregateId(aggregateId)
                .payload(serializeToJson(event))
                .traceId(event.traceId())
                .spanId(tracer.currentSpan() != null ? tracer.currentSpan().context().spanId() : null)
                .processed(false)
                .retryCount(0)
                .build();

        outboxRepository.save(outboxMessage);
    }

    private String serializeToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON-ERROR: Failed to serialize event for aggregate: {}", object);
            throw new RuntimeException("Internal serialization error during event creation");
        }
    }
}