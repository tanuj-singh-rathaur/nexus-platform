package com.rathaur.nexus.portfolioservice.messaging;

import com.rathaur.nexus.common.config.RabbitMQConfig;
import com.rathaur.nexus.common.event.UserRegistrationEvent;
import com.rathaur.nexus.portfolioservice.entity.Profile;
import com.rathaur.nexus.portfolioservice.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/20/2026
 */
@Service
public class ProfileConsumer {

    private final ProfileService profileService;
    private static final Logger log = LoggerFactory.getLogger(ProfileConsumer.class);

    public ProfileConsumer(ProfileService profileService) {
        this.profileService = profileService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleRegistrationEvent(UserRegistrationEvent event) {
        // Trace context is automatically propagated here by Micrometer
        log.info("📩 MQ RECEIVE: Processing registration for: {}", event.username());

        Profile profile = new Profile();
        profile.setUsername(event.username());
        profile.setEmail(event.email());
        profile.setFullName(event.fullName());

        try {
            profileService.createProfile(profile);
            log.info("✅ SUCCESS: Profile created for {}", event.username());
        } catch (Exception e) {
            log.error("❌ ERROR: Failed to initialize profile for {}. Reason: {}",
                    event.username(), e.getMessage());

            // Triggers the Dead Letter Queue movement defined in your common config
            throw new AmqpRejectAndDontRequeueException("Routing to DLQ: " + e.getMessage());
        }
    }
}