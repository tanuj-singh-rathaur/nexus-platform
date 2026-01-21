package com.rathaur.nexus.portfolioservice.exception;

import com.rathaur.nexus.common.dto.ApiError;
import com.rathaur.nexus.common.dto.ApiResponse;
import com.rathaur.nexus.common.exception.BaseExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@RestControllerAdvice
public class PortfolioExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ProfileNotFoundException ex) {
        ApiError error = new ApiError("PORTFOLIO_NOT_FOUND", "The requested profile does not exist.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("Not Found", error, null));
    }

    @ExceptionHandler(ResourceOwnershipException.class)
    public ResponseEntity<ApiResponse<Void>> handleOwnershipError(AccessDeniedException ex) {
        ApiError error = new ApiError("PORTFOLIO_FORBIDDEN", "You do not own this project/profile.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("Access Denied", error, null));
    }
}