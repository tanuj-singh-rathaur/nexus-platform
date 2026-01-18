package com.rathaur.nexus.identityservice.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    // 2. Duplicate User Error (The 403 Fix)
    @ResponseStatus(HttpStatus.CONFLICT) // Returns 409 instead of 403/500
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Map<String, String> handleDuplicateEntry(DataIntegrityViolationException ex) {
        Map<String, String> error = new HashMap<>();
        // You can log the actual ex.getMessage() to console for debugging
        error.put("error", "User already exists! Please check your username or email.");
        return error;
    }
    
    // 3. Catch-All for other errors
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Map<String, String> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "An unexpected error occurred: " + ex.getMessage());
        return error;
    }
}