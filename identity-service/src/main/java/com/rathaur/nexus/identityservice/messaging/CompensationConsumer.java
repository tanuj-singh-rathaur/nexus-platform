package com.rathaur.nexus.identityservice.messaging;

import com.rathaur.nexus.common.event.ProfileCreationFailedEvent;
import com.rathaur.nexus.common.utils.RabbitNames;
import com.rathaur.nexus.identityservice.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for Saga Compensation events.
 * Listens for failures from downstream services to trigger data reversal.
 */
@Component // Must have this for Spring to find it!
public class CompensationConsumer {

    private static final Logger log = LoggerFactory.getLogger(CompensationConsumer.class);
    private final AuthService authService;

    public CompensationConsumer(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Listens to the MAIN failure queue.
     * We only listen to the DLQ for manual recovery/alerts, not for logic.
     */
    @RabbitListener(queues = RabbitNames.PROFILE_FAIL_QUEUE)
    public void handleProfileCreationFailed(ProfileCreationFailedEvent event) {
        log.info("SAGA-COMPENSATION-RECEIVED: Received failure for user [{}]. TraceId: [{}]. Reason: [{}]",
                event.username(), event.traceId(), event.reason());

        try {
            authService.reverseUserRegistration(event.username(), event.reason());
            log.info("SAGA-COMPENSATION-SUCCESS: User [{}] has been removed from Identity DB.", event.username());
        } catch (Exception e) {
            log.error("SAGA-COMPENSATION-ERROR: Failed to reverse registration for user [{}]. Error: {}",
                    event.username(), e.getMessage());
            // This throw will eventually move the message to the DLQ if it fails enough times
            throw e;
        }
    }
}