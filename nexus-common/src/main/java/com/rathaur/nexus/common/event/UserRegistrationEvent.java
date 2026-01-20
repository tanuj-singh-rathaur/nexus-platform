package com.rathaur.nexus.common.event;

import java.io.Serializable;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/20/2026
 */
public record UserRegistrationEvent(String username,String email, String fullName) implements Serializable {
}
