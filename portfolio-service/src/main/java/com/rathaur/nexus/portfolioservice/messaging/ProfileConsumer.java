package com.rathaur.nexus.portfolioservice.messaging;

import com.rathaur.nexus.common.config.RabbitMQConfig;
import com.rathaur.nexus.common.event.UserRegistrationEvent;
import com.rathaur.nexus.portfolioservice.entity.Profile;
import com.rathaur.nexus.portfolioservice.service.ProfileService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/20/2026
 */

@Service
public class ProfileConsumer {

    private final ProfileService profileService;
    public ProfileConsumer(ProfileService profileService){
        this.profileService = profileService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleRegistrationEvent(UserRegistrationEvent event){
        Profile profile = new Profile();
        profile.setUsername(event.username());
        profile.setEmail(event.email());
        profile.setFullName(event.fullName());

        try {
            profileService.createProfile(profile);
            System.out.println("Event Processed: Blank profile created for " + event.username());
        } catch (Exception e) {
            // Log the error (e.g., if the user registered twice quickly)
            System.err.println("Failed to create profile via event: " + e.getMessage());
        }
    }

}
