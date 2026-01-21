package com.rathaur.nexus.identityservice.exception;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
