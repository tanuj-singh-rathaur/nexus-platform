package com.rathaur.nexus.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/20/2026
 */
@Configuration
public class RabbitMQConfig {



    @Value("${spring.application.name:nexus-service}")
    private String applicationName;

    public static final String EXCHANGE = "user.registration.exchange";
    public static final String QUEUE = "user.registration.queue";
    public static final String ROUTING_KEY = "user.registration.routing.key";

    public static final String DLX_EXCHANGE = "user.registration.dlx";
    public static final String DLQ_QUEUE = "user.registration.dlq";

    // --- DEAD LETTER INFRASTRUCTURE ---
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ_QUEUE);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("deadLetter");
    }

    // --- MAIN INFRASTRUCTURE (Merged) ---
    @Bean
    public DirectExchange registrationExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue registrationQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "deadLetter")
                .build();
    }

    @Bean
    public Binding registrationBinding() {
        return BindingBuilder.bind(registrationQueue())
                .to(registrationExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public ConnectionFactory connectionFactory(
            @Value("${spring.rabbitmq.host:localhost}") String host,
            @Value("${spring.rabbitmq.port:5672}") int port,
            @Value("${spring.rabbitmq.username:guest}") String username,
            @Value("${spring.rabbitmq.password:guest}") String password) {

        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);

        // This is the line that replaces "rabbitConnectionFactory#..."
        // with "identity-service" or "portfolio-service"
        factory.setConnectionNameStrategy(connectionFactory -> applicationName);

        return factory;
    }

}
