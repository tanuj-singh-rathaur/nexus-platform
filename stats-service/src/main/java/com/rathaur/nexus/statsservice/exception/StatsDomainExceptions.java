package com.rathaur.nexus.statsservice.exception;

/**
 * Centrally managed domain exceptions for the Stats Service.
 * @author Tanuj Singh Rathaur
 */
public class StatsDomainExceptions {

    /**
     * Thrown when GitHub/LeetCode JSON structure is invalid or missing keys.
     */
    public static class DataParsingException extends RuntimeException {
        public DataParsingException(String message) {
            super(message);
        }
    }

    /**
     * Thrown for general failures during the sync process.
     */
    public static class SyncServiceException extends RuntimeException {
        public SyncServiceException(String message) {
            super(message);
        }

        public SyncServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Thrown when external providers (GitHub/LeetCode) return a 429 Too Many Requests.
     */
    public static class ExternalProviderThrottledException extends RuntimeException {
        public ExternalProviderThrottledException(String message) {
            super(message);
        }
    }
}