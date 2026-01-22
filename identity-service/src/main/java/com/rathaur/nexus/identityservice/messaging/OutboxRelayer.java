package com.rathaur.nexus.identityservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rathaur.nexus.common.event.UserRegistrationEvent;
import com.rathaur.nexus.common.utils.RabbitNames;
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
 * Component responsible for the Transactional Outbox relay.
 * Periodically polls for unprocessed messages and publishes them to RabbitMQ.
 * * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
@Component
public class OutboxRelayer {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private static final Logger log = LoggerFactory.getLogger(OutboxRelayer.class);

    /* Constructor injection ensures proper object lifecycle management */
    public OutboxRelayer(OutboxRepository outboxRepository,
                         RabbitTemplate rabbitTemplate,
                         ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Polls the database for messages that have not been sent to the broker.
     * Transactional at the loop level to ensure individual message status persistence.
     */
    @Scheduled(fixedDelayString = "${nexus.outbox.relay-delay:5000}")
    @Transactional
    public void relayMessages() {
        List<OutboxMessage> pendingMessages = outboxRepository.findByProcessedFalse();
        if (pendingMessages.isEmpty()) {
            return;
        }

        log.debug("Outbox: Found {} pending messages to relay", pendingMessages.size());

        for (OutboxMessage message : pendingMessages) {
            try {
                /* Deserialize the payload back into the shared event contract */
                UserRegistrationEvent event = objectMapper.readValue(
                        message.getPayload(),
                        UserRegistrationEvent.class
                );

                /* Publish to RabbitMQ using the common utility names */
                rabbitTemplate.convertAndSend(
                        RabbitNames.USER_REG_EXCHANGE,
                        RabbitNames.USER_REG_ROUTING_KEY,
                        event,
                        m -> {
                            /* Propagate tracing context to maintain distributed observability */
                            if (message.getTraceId() != null) {
                                m.getMessageProperties().setHeader("X-B3-TraceId", message.getTraceId());
                                m.getMessageProperties().setHeader("X-B3-SpanId", message.getSpanId());

                                // This forces the consumer to keep the trace (don't sample it out)
                                m.getMessageProperties().setHeader("X-B3-Sampled", "1");
                            }
                            return m;
                        }
                );

                message.setProcessed(true);
                message.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(message);

                log.info("Outbox: Successfully relayed message for aggregate: {}", message.getAggregateId());
            } catch (Exception e) {
                log.error("Outbox: Failed to relay message {}. Error: {}", message.getId(), e.getMessage());
                /* * In a senior-level app, you might increment a 'retry_count' on the entity here
                 * to implement an 'Exponential Backoff' before sending it to a dead-letter table.
                 */

            }
        }
    }

    /**
     * Nightly cleanup to purge successfully processed messages older than 24 hours.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanUpOutbox() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        log.info("Outbox Cleanup: Initiating purge for messages older than {}", cutoff);

        try {
            outboxRepository.deleteProcessedMessagesOlderThan(cutoff);
            log.info("Outbox Cleanup: Purge completed successfully");
        } catch (Exception e) {
            log.error("Outbox Cleanup: Failed to purge processed messages", e);
        }
    }
}