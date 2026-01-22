package com.rathaur.nexus.identityservice.config;

import com.rathaur.nexus.common.utils.RabbitNames;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Identity-Specific Messaging Configuration.
 * Owns the consumer queues for Saga Compensation (undoing registration).
 */
@Configuration
public class ProfileFailureRabbitConfig {

    /**
     * The primary queue for Saga failure events.
     * If Identity fails to "undo" a user, it routes to its own DLQ.
     */
    @Bean
    public Queue profileFailQueue() {
        return QueueBuilder.durable(RabbitNames.PROFILE_FAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitNames.PROFILE_FAIL_DLX)
                .withArgument("x-dead-letter-routing-key", RabbitNames.PROFILE_FAIL_DL_RK)
                .build();
    }

    /**
     * DLQ for the compensation flow itself.
     */
    @Bean
    public Queue profileFailDeadLetterQueue() {
        return QueueBuilder.durable(RabbitNames.PROFILE_FAIL_DLQ).build();
    }

    @Bean
    public Binding profileFailBinding(Queue profileFailQueue, DirectExchange profileFailExchange) {
        return BindingBuilder.bind(profileFailQueue)
                .to(profileFailExchange)
                .with(RabbitNames.PROFILE_FAIL_ROUTING_KEY);
    }

    @Bean
    public Binding profileFailDLQBinding(Queue profileFailDeadLetterQueue, DirectExchange profileFailDLX) {
        return BindingBuilder.bind(profileFailDeadLetterQueue)
                .to(profileFailDLX)
                .with(RabbitNames.PROFILE_FAIL_DLQ);
    }
}