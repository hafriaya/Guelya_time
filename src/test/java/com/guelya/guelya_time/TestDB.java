package com.guelya.guelya_time;

import model.User;
import service.UserService;

import java.util.Optional;

public class TestDB {
    public static void main(String[] args) {
        UserService userService = new UserService();

        try {
            // Try to register a new user
            User newUser = userService.register("testuser", "test@example.com", "Password123");
            System.out.println("User registered successfully: " + newUser.getUsername());

            // Try to login
            Optional<User> loggedInUser = userService.login("testuser", "Password123");
            if (loggedInUser.isPresent()) {
                System.out.println("Login successful: " + loggedInUser.get().getUsername());
            } else {
                System.out.println("Login failed");
            }

        } catch (UserService.RegistrationException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }
}