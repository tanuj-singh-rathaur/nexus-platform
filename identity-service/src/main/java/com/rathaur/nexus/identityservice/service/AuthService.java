package com.rathaur.nexus.identityservice.service;

import com.rathaur.nexus.common.config.RabbitMQConfig;
import com.rathaur.nexus.common.event.UserRegistrationEvent;
import com.rathaur.nexus.common.security.JwtUtils;
import com.rathaur.nexus.identityservice.entity.UserCredential;
import com.rathaur.nexus.identityservice.repository.UserCredentialRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/17/2026
 */
@Service
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private RabbitTemplate rabbitTemplate;


    public AuthService(UserCredentialRepository userCredentialRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, RabbitTemplate rabbitTemplate){
        this.userCredentialRepository = userCredentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils =jwtUtils;
        this.rabbitTemplate = rabbitTemplate;
    }

    public String saveUser(UserCredential credential){
        credential.setPassword(passwordEncoder.encode(credential.getPassword()));
        userCredentialRepository.save(credential);

        UserRegistrationEvent event =  new UserRegistrationEvent(credential.getName(), credential.getEmail(), credential.getPassword());

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
        System.out.println("Registration event published for: " + credential.getName());
        return "User Added to the system and registration and Registration event published for:" + credential.getName();
    }

    public String generateToken(String username){
        return jwtUtils.generateToken(username);
    }
}
