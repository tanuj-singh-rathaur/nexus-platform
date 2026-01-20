package com.rathaur.nexus.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/20/2026
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "outbox_messages")
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateId; // This will be the username

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;    // The UserRegistrationEvent serialized as JSON

    private String traceId;
    private String spanId;

    @Column(nullable = false)
    private boolean processed = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}