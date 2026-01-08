package com.guelyatime.dto;

import com.guelyatime.model.User;

/**
 * DTO pour la réponse d'authentification
 */
public class AuthResponse {

    private User user;
    private String token;
    private String message;

    // Constructeurs
    public AuthResponse() {}

    public AuthResponse(User user, String token, String message) {
        this.user = user;
        this.token = token;
        this.message = message;
    }

    public static AuthResponse success(User user, String token) {
        return new AuthResponse(user, token, "Opération réussie");
    }

    public static AuthResponse registered(User user) {
        return new AuthResponse(user, null, "Inscription réussie");
    }

    // Getters et Setters
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
