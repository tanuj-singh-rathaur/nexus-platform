package com.rathaur.nexus.identityservice.repository;

import com.rathaur.nexus.identityservice.entity.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/20/2026
 */
@Repository
public interface OutboxRepository extends JpaRepository<OutboxMessage, Long> {

    // Used by the background worker to find messages to publish
    List<OutboxMessage> findByProcessedFalse();

    @Modifying
    @Query("DELETE FROM OutboxMessage o WHERE o.processed = true AND o.createdAt < :cutoff")
    void deleteProcessedMessagesOlderThan(LocalDateTime cutoff);
}