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

    public static final String EXCHANGE="user.registration.exchange";
    public static final String QUEUE="user.registration.queue";
    public static final String ROUTING_KEY="user.registration.routing.key";

    @Value("${spring.application.name:nexus-service}")
    private String applicationName;

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

    @Bean
    public TopicExchange registrationExchange(){
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue registrationQueue() {
        return new Queue(QUEUE);
    }

    @Bean
    public Binding registrationBinding(){
        return BindingBuilder.bind(registrationQueue()).to(registrationExchange()).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(){
        return new JacksonJsonMessageConverter();
    }

}
