package com.rathaur.nexus.identityservice.exception;

/**
 * Centrally managed domain exceptions for the Identity Service.
 * Grouping these as static inner classes keeps the 'exception' package clean.
 * * @author Tanuj Singh Rathaur
 */
public class IdentityDomainExceptions {

    /**
     * CONFLICT (409): Username or Email already taken.
     */
    public static class UserAlreadyExistsException extends RuntimeException {
        public UserAlreadyExistsException(String message) {
            super(message);
        }
    }

    /**
     * NOT FOUND (404): Generic resource missing (e.g., a specific User profile).
     */
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * INTERNAL ERROR (500): Database is missing a required role like ROLE_USER.
     */
    public static class RoleNotFoundException extends RuntimeException {
        public RoleNotFoundException(String message) {
            super(message);
        }
    }

    // --- RECOMMENDED ADDITIONS FOR PRODUCTION ---

    /**
     * FORBIDDEN (403): User exists but hasn't verified their email yet.
     */
    public static class AccountDisabledException extends RuntimeException {
        public AccountDisabledException(String message) {
            super(message);
        }
    }

    /**
     * FORBIDDEN (403): Too many failed login attempts (Brute force protection).
     */
    public static class AccountLockedException extends RuntimeException {
        public AccountLockedException(String message) {
            super(message);
        }
    }

    /**
     * UNAUTHORIZED (401): Refresh token is invalid or has been revoked.
     */
    public static class InvalidRefreshTokenException extends RuntimeException {
        public InvalidRefreshTokenException(String message) {
            super(message);
        }
    }
}