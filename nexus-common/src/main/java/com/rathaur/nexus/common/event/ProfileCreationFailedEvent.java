package com.rathaur.nexus.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event representing a failure in profile initialization.
 * Used to trigger Saga Compensation in the Identity Service.
 * * @author Tanuj Singh Rathaur
 * @date 1/22/2026
 */
public record ProfileCreationFailedEvent(
        @JsonProperty("username") String username,
        @JsonProperty("reason") String reason,


        // --- Standard Metadata (IDENTICAL TO ABOVE) ---
        @JsonProperty("eventId") String eventId,
        @JsonProperty("traceId") String traceId,
        @JsonProperty("occurredAt") LocalDateTime occurredAt

) implements Serializable {

    /**
     * Static Factory Method to ensure all required metadata is captured.
     * @param username The identity to be reversed
     * @param reason   Why the creation failed
     * @param traceId  The original trace ID from the registration flow
     */
    public static ProfileCreationFailedEvent create(String username, String reason, String traceId) {
        return new ProfileCreationFailedEvent(
                username,
                reason,
                UUID.randomUUID().toString(),
                traceId,
                LocalDateTime.now()
        );
    }
}