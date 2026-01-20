package com.rathaur.nexus.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
