package com.rathaur.nexus.portfolioservice.config;

import com.rathaur.nexus.common.utils.RabbitNames;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Portfolio-Specific Messaging Configuration.
 * Owns the consumer queues for incoming user registrations.
 */
@Configuration
public class UserRegistrationRabbitConfig {

    @Bean
    public Queue userRegistrationQueue() {
        return QueueBuilder.durable(RabbitNames.USER_REG_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitNames.USER_REG_DLX)
                .withArgument("x-dead-letter-routing-key", RabbitNames.USER_REG_DL_RK)
                .build();
    }

    @Bean
    public Queue userRegistrationDeadLetterQueue() {
        return QueueBuilder.durable(RabbitNames.USER_REG_DLQ).build();
    }

    @Bean
    public Binding userRegistrationBinding(Queue userRegistrationQueue, DirectExchange userRegistrationExchange) {
        return BindingBuilder.bind(userRegistrationQueue)
                .to(userRegistrationExchange)
                .with(RabbitNames.USER_REG_ROUTING_KEY);
    }

    @Bean
    public Binding userRegistrationDeadLetterBinding(Queue userRegistrationDeadLetterQueue, DirectExchange userRegistrationDLX) {
        return BindingBuilder.bind(userRegistrationDeadLetterQueue)
                .to(userRegistrationDLX)
                .with(RabbitNames.USER_REG_DL_RK);
    }
}