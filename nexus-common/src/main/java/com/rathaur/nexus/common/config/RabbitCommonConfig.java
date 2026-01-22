package com.rathaur.nexus.common.config;

import com.rathaur.nexus.common.utils.RabbitNames;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global RabbitMQ Infrastructure.
 * Defines shared exchanges and connection logic for all Nexus microservices.
 */
@Configuration
public class RabbitCommonConfig {

    @Value("${spring.application.name:nexus-service}")
    private String applicationName;

    /**
     * Standard JSON converter to ensure Java objects are serialized properly across services.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    /* --- Shared Exchanges for User Registration Flow --- */

    @Bean
    public DirectExchange userRegistrationExchange() {
        return new DirectExchange(RabbitNames.USER_REG_EXCHANGE);
    }

    @Bean
    public DirectExchange userRegistrationDLX() {
        return new DirectExchange(RabbitNames.USER_REG_DLX);
    }

    /* --- Shared Exchanges for Saga Compensation Flow --- */

    @Bean
    public DirectExchange profileFailExchange() {
        return new DirectExchange(RabbitNames.PROFILE_FAIL_EXCHANGE);
    }

    @Bean
    public DirectExchange profileFailDLX() {
        return new DirectExchange(RabbitNames.PROFILE_FAIL_DLX);
    }

    /**
     * Centralized Connection Factory.
     * ConnectionNameStrategy helps identify which microservice is connected in the RabbitMQ Dashboard.
     */
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