package com.rathaur.nexus.portfolioservice.exception;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(String message) {
        super(message);
    }
}