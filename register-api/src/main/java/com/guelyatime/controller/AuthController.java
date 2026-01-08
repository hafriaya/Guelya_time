package com.guelyatime.controller;

import com.guelyatime.dto.AuthResponse;
import com.guelyatime.dto.LoginRequest;
import com.guelyatime.dto.RegisterRequest;
import com.guelyatime.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'authentification
 * 
 * Endpoints:
 * - POST /api/auth/register : Inscription d'un nouvel utilisateur
 * - POST /api/auth/login    : Connexion d'un utilisateur
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Inscription d'un nouvel utilisateur
     * 
     * @param request Les données d'inscription
     * @return L'utilisateur créé avec un message de succès
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Connexion d'un utilisateur
     * 
     * @param request Les identifiants de connexion
     * @return L'utilisateur avec son token JWT
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de test pour vérifier que l'API est accessible
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Auth service is running");
    }
}
