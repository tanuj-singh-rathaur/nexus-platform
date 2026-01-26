package com.rathaur.nexus.portfolioservice.exception;

/**
 * Centrally managed domain exceptions for the Portfolio Service.
 * @author Tanuj Singh Rathaur
 */
public class PortfolioDomainExceptions {

    /**
     * NOT FOUND (404): Thrown when a profile, project, or skill doesn't exist.
     */
    public static class ProfileNotFoundException extends RuntimeException {
        public ProfileNotFoundException(String message) { super(message); }
    }

    /**
     * FORBIDDEN (403): Thrown when a user tries to edit a project they don't own.
     */
    public static class ResourceOwnershipException extends RuntimeException {
        public ResourceOwnershipException(String message) { super(message); }
    }

    /**
     * UNPROCESSABLE (422): Thrown if the provided portfolio data is logically invalid.
     */
    public static class PortfolioDataException extends RuntimeException {
        public PortfolioDataException(String message) { super(message); }
    }
}