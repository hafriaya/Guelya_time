package com.guelyatime.exception;

/**
 * Exception levée quand un email existe déjà
 */
public class EmailAlreadyExistsException extends RuntimeException {

    private final String field = "email";

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public String getField() {
        return field;
    }
}
