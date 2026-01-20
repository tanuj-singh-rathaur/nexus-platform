package com.rathaur.nexus.identityservice.messaging;

import com.rathaur.nexus.common.config.RabbitMQConfig;
import com.rathaur.nexus.common.event.UserRegistrationEvent;
import com.rathaur.nexus.identityservice.entity.OutboxMessage;
import com.rathaur.nexus.identityservice.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/20/2026
 */
@Component
public class OutboxRelayer {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private static final Logger log = LoggerFactory.getLogger(OutboxRelayer.class);

    public OutboxRelayer(OutboxRepository outboxRepository, RabbitTemplate rabbitTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // 1. Add ObjectMapper to your class
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void relayMessages() {
        List<OutboxMessage> pendingMessages = outboxRepository.findByProcessedFalse();
        if (pendingMessages.isEmpty()) return;

        for (OutboxMessage message : pendingMessages) {
            try {
                // ✅ Fix: Convert the String payload back into the Event Object
                UserRegistrationEvent event = objectMapper.readValue(
                        message.getPayload(),
                        UserRegistrationEvent.class
                );

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE,
                        RabbitMQConfig.ROUTING_KEY,
                        event, // 👈 Pass the 'event' object, not 'message.getPayload()'
                        (m) -> {
                            if (message.getTraceId() != null) {
                                m.getMessageProperties().setHeader("X-B3-TraceId", message.getTraceId());
                                m.getMessageProperties().setHeader("X-B3-SpanId", message.getSpanId());
                                m.getMessageProperties().setHeader("X-B3-Sampled", "1");
                            }
                            return m;
                        }
                );

                message.setProcessed(true);
                outboxRepository.save(message);
                log.info("✅ Outbox: Successfully relayed message for: {}", message.getAggregateId());
            } catch (Exception e) {
                log.error("❌ Outbox: Failed to relay message {}: {}", message.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 86400000)
    @Transactional
    public void cleanUpOutbox() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        log.info("🧹 Outbox Cleanup: Removing processed messages older than {}", cutoff);

        try {
            outboxRepository.deleteProcessedMessagesOlderThan(cutoff);
            log.info("✅ Outbox Cleanup: Completed successfully.");
        } catch (Exception e) {
            log.error("❌ Outbox Cleanup: Failed to purge old messages", e);
        }
    }


}