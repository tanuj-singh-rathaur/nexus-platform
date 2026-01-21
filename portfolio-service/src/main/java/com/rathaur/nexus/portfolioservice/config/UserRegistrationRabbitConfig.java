package com.rathaur.nexus.portfolioservice.config;

import com.rathaur.nexus.common.utils.RabbitNames;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ Implementation for the Portfolio Service.
 * Defines the specific Queue and Bindings required to consume
 * User Registration events from the Identity Service.
 *
 * @author Tanuj Singh Rathaur
 */
@Configuration
public class UserRegistrationRabbitConfig {

    /**
     * The primary queue where user registration messages land.
     * Configured with a Dead Letter Exchange (DLX) to handle processing failures.
     */
    @Bean
    public Queue userRegistrationQueue() {
        return QueueBuilder.durable(RabbitNames.USER_REGISTRATION_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitNames.USER_REGISTRATION_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitNames.USER_REGISTRATION_DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    /**
     * The "Hospital Queue" (DLQ). Messages that fail in the main queue
     * are automatically routed here for manual inspection.
     */
    @Bean
    public Queue userRegistrationDeadLetterQueue() {
        return QueueBuilder.durable(RabbitNames.USER_REGISTRATION_DEAD_LETTER_QUEUE).build();
    }

    /**
     * Binds the Main Queue to the Main Exchange using the specific Routing Key.
     * This is the "Link" that ensures messages from Identity reach Portfolio.
     */
    @Bean
    public Binding userRegistrationBinding(Queue userRegistrationQueue,
                                           DirectExchange userRegistrationExchange) {
        return BindingBuilder.bind(userRegistrationQueue)
                .to(userRegistrationExchange)
                .with(RabbitNames.USER_REGISTRATION_ROUTING_KEY);
    }

    /**
     * Binds the DLQ to the Dead Letter Exchange.
     * When a message is "Dead Lettered", it uses the DL-Routing-Key to find this queue.
     */
    @Bean
    public Binding deadLetterBinding(Queue userRegistrationDeadLetterQueue,
                                     DirectExchange userRegistrationDLX) {
        return BindingBuilder.bind(userRegistrationDeadLetterQueue)
                .to(userRegistrationDLX)
                .with(RabbitNames.USER_REGISTRATION_DEAD_LETTER_ROUTING_KEY);
    }
}