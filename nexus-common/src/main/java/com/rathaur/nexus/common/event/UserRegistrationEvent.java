package com.rathaur.nexus.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event representing a successful user credential creation.
 * Optimized as a Java Record for immutability.
 */
public record UserRegistrationEvent(
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("fullName") String fullName,
        @JsonProperty("role") String role,
        @JsonProperty("eventId") String eventId,
        @JsonProperty("traceId") String traceId,
        @JsonProperty("occurredAt") LocalDateTime occurredAt
) implements Serializable {

    public UserRegistrationEvent {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
    }


    public static UserRegistrationEvent create(String username, String email, String name, String role, String traceId) {
        return new UserRegistrationEvent(
                username,
                email,
                name,
                role,
                UUID.randomUUID().toString(),
                traceId,
                LocalDateTime.now()
        );
    }
}