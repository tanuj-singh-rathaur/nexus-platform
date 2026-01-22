package com.rathaur.nexus.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Entity for the Transactional Outbox pattern.
 * Ensures reliable message delivery between Identity and Portfolio services.
 * * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "outbox_messages", indexes = {
        @Index(name = "idx_outbox_processed", columnList = "processed"),
        @Index(name = "idx_outbox_created_at", columnList = "createdAt")
})
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* The unique business identifier (e.g., username or userId) */
    @Column(nullable = false)
    private String aggregateId;

    /* Serialized JSON payload of the event */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    /* Distributed Tracing context for cross-service observability */
    private String traceId;
    private String spanId;

    /* Status tracking */
    @Builder.Default
    @Column(nullable = false)
    private boolean processed = false;

    /* Resilience: Track retry attempts to handle 'poison' messages */
    @Builder.Default
    @Column(nullable = false)
    private int retryCount = 0;

    /* Auditing timestamps */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    /**
     * Automatically sets the creation timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}