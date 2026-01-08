package com.guelyatime.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO pour les réponses d'erreur API
 */
public class ErrorResponse {

    private String message;
    private String field;
    private Map<String, String> errors;
    private int status;

    // Constructeurs
    public ErrorResponse() {
        this.errors = new HashMap<>();
    }

    public ErrorResponse(String message) {
        this();
        this.message = message;
    }

    public ErrorResponse(String message, int status) {
        this(message);
        this.status = status;
    }

    public ErrorResponse(String message, String field) {
        this(message);
        this.field = field;
    }

    // Méthodes factory
    public static ErrorResponse of(String message) {
        return new ErrorResponse(message);
    }

    public static ErrorResponse of(String message, int status) {
        return new ErrorResponse(message, status);
    }

    public static ErrorResponse validationError(Map<String, String> errors) {
        ErrorResponse response = new ErrorResponse("Erreur de validation");
        response.setErrors(errors);
        response.setStatus(400);
        return response;
    }

    public ErrorResponse addError(String field, String message) {
        this.errors.put(field, message);
        return this;
    }

    // Getters et Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
