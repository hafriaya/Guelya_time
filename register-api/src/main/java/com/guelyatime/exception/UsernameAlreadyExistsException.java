package com.guelyatime.exception;

/**
 * Exception levée quand un nom d'utilisateur existe déjà
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    private final String field = "username";

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }

    public String getField() {
        return field;
    }
}
