package com.rathaur.nexus.common.config;

import com.rathaur.nexus.common.utils.RabbitNames;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Common RabbitMQ configuration for all Nexus microservices.
 * Implements the Dead Letter Pattern for production resilience.
 */
@Configuration
public class RabbitCommonConfig {

    @Value("${spring.application.name:nexus-service}")
    private String applicationName;

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public DirectExchange userRegistrationExchange() {
        return new DirectExchange(RabbitNames.USER_REGISTRATION_EXCHANGE);
    }

    @Bean
    public DirectExchange userRegistrationDLX() {
        return new DirectExchange(RabbitNames.USER_REGISTRATION_DEAD_LETTER_EXCHANGE);
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
        factory.setConnectionNameStrategy(cf -> applicationName);
        return factory;
    }
}