package com.rathaur.nexus.portfolioservice.exception;

import com.rathaur.nexus.common.dto.ApiError;
import com.rathaur.nexus.common.dto.ApiResponse;
import com.rathaur.nexus.common.exception.BaseExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global Exception Handler for Portfolio Service.
 * Inherits Global Infrastructure handlers (JWT, Validation) from BaseExceptionHandler.
 */
@RestControllerAdvice
public class PortfolioExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(PortfolioDomainExceptions.ProfileNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(PortfolioDomainExceptions.ProfileNotFoundException ex) {
        ApiError error = new ApiError("PORTFOLIO_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("Resource Not Found", error, getTraceId()));
    }

    @ExceptionHandler(PortfolioDomainExceptions.ResourceOwnershipException.class)
    public ResponseEntity<ApiResponse<Void>> handleOwnershipError(PortfolioDomainExceptions.ResourceOwnershipException ex) {
        ApiError error = new ApiError("PORTFOLIO_FORBIDDEN", "You do not have permission to modify this resource.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail("Access Denied", error, getTraceId()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataConflict(DataIntegrityViolationException ex) {
        ApiError error = new ApiError("PORTFOLIO_CONFLICT", "This data entry already exists or violates constraints.");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail("Data Conflict", error, getTraceId()));
    }

    @ExceptionHandler(PortfolioDomainExceptions.PortfolioDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidData(PortfolioDomainExceptions.PortfolioDataException ex) {
        ApiError error = new ApiError("PORTFOLIO_INVALID_DATA", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.fail("Logical Error", error, getTraceId()));
    }
}