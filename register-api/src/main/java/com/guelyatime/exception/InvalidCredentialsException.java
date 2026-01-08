package com.guelyatime.exception;

/**
 * Exception levée quand les identifiants sont invalides
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
