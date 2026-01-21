package com.rathaur.nexus.identityservice.entity;

/**
 * @author Tanuj Singh Rathaur
 * @date 1/21/2026
 */
public enum Role {
    ROLE_USER,      // Standard SaaS customer
    ROLE_PRO,       // Paid tier customer
    ROLE_ADMIN,     // Your internal staff
    ROLE_OWNER
}
