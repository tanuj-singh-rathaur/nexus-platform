package com.rathaur.nexus.identityservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rathaur.nexus.common.event.UserRegistrationEvent;
import com.rathaur.nexus.common.security.JwtUtils;
import com.rathaur.nexus.common.utils.SecurityConstants;
import com.rathaur.nexus.identityservice.dto.AuthRequest;
import com.rathaur.nexus.identityservice.dto.AuthTokenResponse;
import com.rathaur.nexus.identityservice.entity.OutboxMessage;
import com.rathaur.nexus.identityservice.entity.Role;
import com.rathaur.nexus.identityservice.entity.UserCredential;
import com.rathaur.nexus.identityservice.exception.IdentityDomainExceptions;
import com.rathaur.nexus.identityservice.repository.OutboxRepository;
import com.rathaur.nexus.identityservice.repository.UserCredentialRepository;
import io.micrometer.tracing.Tracer;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service handling Identity and Access Management (IAM) logic.
 * Implements Transactional Outbox and utilizes unified Identity Domain Exceptions.
 * * @author Tanuj Singh Rathaur
 * @date 01/26/2026
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Value("${nexus.auth.access-expiry-ms:600000}") // Default 10 min
    private long accessExpiryMs;

    @Value("${nexus.auth.refresh-expiry-ms:1296000000}") // Default 15 days
    private long refreshExpiryMs;

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

    /**
     * Handles user login logic and token generation.
     * Utilizes AccountLocked and AccountDisabled checks.
     */
    public AuthTokenResponse login(Authentication auth, String username) {
        UserCredential user = userCredentialRepository.findByUsername(username)
                .orElseThrow(() -> new IdentityDomainExceptions.ResourceNotFoundException("User not found: " + username));

        // Logical check for Account Status (Utilizing our new exceptions)
        if (user.isLocked()) {
            throw new IdentityDomainExceptions.AccountLockedException("This account has been locked.");
        }
        if (!user.isEnabled()) {
            throw new IdentityDomainExceptions.AccountDisabledException("Account is disabled. Please verify your email.");
        }

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = generateAccessToken(username, roles);
        String refreshToken = generateRefreshToken(username);

        log.info("AUTH-SUCCESS: Tokens generated for user: {}", username);

        return new AuthTokenResponse(
                "Bearer",
                accessToken,
                refreshToken,
                accessExpiryMs / 1000,
                username,
                roles
        );
    }

    @Transactional
    public String saveUser(AuthRequest request) {
        validateUserUniqueness(request.getUsername(), request.getEmail());

        UserCredential credential = mapToEntity(request);
        userCredentialRepository.save(credential);

        String currentTraceId = getEffectiveTraceId();

        UserRegistrationEvent event = UserRegistrationEvent.create(
                credential.getUsername(),
                credential.getEmail(),
                credential.getName(),
                credential.getRole().name(),
                currentTraceId
        );

        persistOutboxMessage(credential.getUsername(), event, currentTraceId);

        log.info("IDENTITY-SERVICE: Registered user [{}] with TraceID [{}]", credential.getUsername(), currentTraceId);
        return "Registration successful for " + credential.getUsername();
    }

    @Transactional
    public void reverseUserRegistration(String username, String reason) {
        log.warn("SAGA-COMPENSATION: Reversing registration for user: {}. Reason: {}", username, reason);
        userCredentialRepository.findByUsername(username).ifPresentOrElse(
                userCredentialRepository::delete,
                () -> log.error("SAGA-COMPENSATION-ERROR: User {} not found for reversal", username)
        );
    }

    public void validateToken(String token) {
        try {
            jwtUtils.parseAndValidate(token);
        } catch (Exception e) {
            log.error("TOKEN-VALIDATION-FAILURE: {}", e.getMessage());
            throw new IdentityDomainExceptions.InvalidRefreshTokenException("Token validation failed: " + e.getMessage());
        }
    }

    public String generateAccessToken(String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_ACCESS);
        claims.put(SecurityConstants.CLAIM_ROLES, roles);
        return jwtUtils.generateToken(claims, username, accessExpiryMs);
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.CLAIM_TOKEN_TYPE, SecurityConstants.TOKEN_TYPE_REFRESH);
        return jwtUtils.generateToken(claims, username, refreshExpiryMs);
    }

    private void validateUserUniqueness(String username, String email) {
        if (userCredentialRepository.findByUsername(username).isPresent()) {
            throw new IdentityDomainExceptions.UserAlreadyExistsException("Username '" + username + "' is already taken.");
        }
        if (userCredentialRepository.findByEmail(email).isPresent()) {
            throw new IdentityDomainExceptions.UserAlreadyExistsException("Email '" + email + "' is already registered.");
        }
    }

    private UserCredential mapToEntity(AuthRequest request) {
        UserCredential credential = new UserCredential();
        credential.setName(request.getName());
        credential.setUsername(request.getUsername());
        credential.setEmail(request.getEmail());
        credential.setPassword(passwordEncoder.encode(request.getPassword()));
        credential.setRole(Role.ROLE_USER); // Could be updated to RoleNotFoundException check if using DB roles
        credential.setEnabled(true); // Default to true for now
        credential.setLocked(false);
        return credential;
    }

    private void persistOutboxMessage(String aggregateId, UserRegistrationEvent event, String traceId) {
        OutboxMessage outboxMessage = OutboxMessage.builder()
                .aggregateId(aggregateId)
                .payload(serializeToJson(event))
                .traceId(traceId)
                .spanId(tracer.currentSpan() != null ? tracer.currentSpan().context().spanId() : null)
                .processed(false)
                .retryCount(0)
                .build();

        outboxRepository.save(outboxMessage);
    }

    private String getEffectiveTraceId() {
        return (tracer.currentSpan() != null)
                ? tracer.currentSpan().context().traceId()
                : "internal-" + UUID.randomUUID();
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