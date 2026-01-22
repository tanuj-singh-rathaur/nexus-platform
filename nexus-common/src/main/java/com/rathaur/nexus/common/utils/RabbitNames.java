package com.rathaur.nexus.common.utils;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
public final class RabbitNames {
    private RabbitNames() {}

    // --- USER REGISTRATION FLOW (Forward Saga) ---
    public static final String USER_REG_EXCHANGE = "nexus.user.reg.exchange";
    public static final String USER_REG_QUEUE = "nexus.user.reg.queue";
    public static final String USER_REG_ROUTING_KEY = "nexus.user.reg.key";

    public static final String USER_REG_DLX = "nexus.user.reg.dlx";
    public static final String USER_REG_DLQ = "nexus.user.reg.dlq";
    public static final String USER_REG_DL_RK = "nexus.user.reg.dl.key";

    // --- PROFILE FAILURE FLOW (Compensating Saga) ---
    public static final String PROFILE_FAIL_EXCHANGE = "nexus.profile.fail.exchange";
    public static final String PROFILE_FAIL_QUEUE = "nexus.profile.fail.queue";
    public static final String PROFILE_FAIL_ROUTING_KEY = "nexus.profile.fail.key";

    public static final String PROFILE_FAIL_DLX = "nexus.profile.fail.dlx";
    public static final String PROFILE_FAIL_DLQ = "nexus.profile.fail.dlq";
    public static final String PROFILE_FAIL_DL_RK = "nexus.profile.fail.dl.key";
}

