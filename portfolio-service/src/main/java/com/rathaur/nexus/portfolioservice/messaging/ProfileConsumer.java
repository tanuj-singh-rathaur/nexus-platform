package com.rathaur.nexus.portfolioservice.messaging;

import com.rathaur.nexus.common.event.ProfileCreationFailedEvent;
import com.rathaur.nexus.common.event.UserRegistrationEvent;
import com.rathaur.nexus.common.utils.RabbitNames;
import com.rathaur.nexus.portfolioservice.entity.Profile;
import com.rathaur.nexus.portfolioservice.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

/**
 * Consumer for User Registration Events.
 * Bridges the Identity Service and Portfolio Service asynchronously.
 * * @author Tanuj Singh Rathaur
 *
 * @date 1/21/2026
 */
@Service
public class ProfileConsumer {

    private final ProfileService profileService;
    private final RabbitTemplate rabbitTemplate;
    private static final Logger log = LoggerFactory.getLogger(ProfileConsumer.class);

    public ProfileConsumer(ProfileService profileService, RabbitTemplate rabbitTemplate) {
        this.profileService = profileService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitNames.USER_REG_QUEUE)
    public void handleRegistrationEvent(UserRegistrationEvent event, Message message) {
        /* Micrometer Tracing automatically maps headers back to the current MDC context here */
        log.info("MQ RECEIVE: Processing registration for user: {}", event.username());
        Object trace = message.getMessageProperties().getHeaders().get("X-B3-TraceId");
        Object span = message.getMessageProperties().getHeaders().get("X-B3-SpanId");
        Object b3 = message.getMessageProperties().getHeaders().get("b3");

        try {
            /* Map the Event Record to a Profile Entity */
            Profile profile = new Profile();
            profile.setUsername(event.username());
            profile.setEmail(event.email());
            profile.setFullName(event.fullName());

            /* * The service method is already idempotent (it checks if profile exists),
             * which is critical if RabbitMQ delivers the same message twice.
             */
            profileService.createProfile(profile);
            log.info("RABBIT_HEADERS traceId={} spanId={} b3={}", trace, span, b3);
            log.info("SUCCESS: Profile initialized for user: {}", event.username());
        } catch (Exception e) {
            log.error("FAILURE: Could not create profile for user {}. Reason: {}",
                    event.username(), e.getMessage());

            ProfileCreationFailedEvent profileCreationFailedEvent = ProfileCreationFailedEvent.create(
                    event.username(),
                    e.getMessage(),
                    event.traceId()
            );
            rabbitTemplate.convertAndSend(
                    RabbitNames.PROFILE_FAIL_EXCHANGE,
                    RabbitNames.PROFILE_FAIL_ROUTING_KEY,
                    profileCreationFailedEvent
            );

            /* * Throwing this specific exception ensures the message is moved to
             * the DLQ (nexus.user.registration.dlq) instead of retrying forever.
             */
            throw new AmqpRejectAndDontRequeueException("Permanent failure, moving to DLQ", e);
        }
    }
}