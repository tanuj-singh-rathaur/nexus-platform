package com.rathaur.nexus.common.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/20/2026
 */
public record UserRegistrationEvent(@JsonProperty("username") String username,
                                    @JsonProperty("email") String email,
                                    @JsonProperty("fullName") String fullName,
                                    @JsonProperty("role") String role
) implements Serializable {
}