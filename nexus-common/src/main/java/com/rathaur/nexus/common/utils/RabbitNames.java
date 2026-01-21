package com.rathaur.nexus.common.utils;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
public final class RabbitNames {
    private RabbitNames() {}

    // ===== User Registration Event =====
    public static final String USER_REGISTRATION_EXCHANGE = "nexus.user.registration.exchange";
    public static final String USER_REGISTRATION_ROUTING_KEY = "nexus.user.registration.routing-key";

    // Main Consumer Queue
    public static final String USER_REGISTRATION_QUEUE = "nexus.user.registration.main-queue";

    // Dead-Lettering (The Error Handling Infrastructure)
    public static final String USER_REGISTRATION_DEAD_LETTER_EXCHANGE = "nexus.user.registration.dlx";
    public static final String USER_REGISTRATION_DEAD_LETTER_QUEUE = "nexus.user.registration.dlq";
    public static final String USER_REGISTRATION_DEAD_LETTER_ROUTING_KEY = "nexus.user.registration.dead-letter-routing-key";
}

