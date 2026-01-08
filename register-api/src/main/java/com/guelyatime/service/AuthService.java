package com.guelyatime.service;

import com.guelyatime.dto.RegisterRequest;
import com.guelyatime.dto.LoginRequest;
import com.guelyatime.dto.AuthResponse;
import com.guelyatime.exception.EmailAlreadyExistsException;
import com.guelyatime.exception.UsernameAlreadyExistsException;
import com.guelyatime.exception.InvalidCredentialsException;
import com.guelyatime.model.User;
import com.guelyatime.repository.UserRepository;
import com.guelyatime.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service d'authentification pour l'inscription et la connexion
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Inscription d'un nouvel utilisateur
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si le nom d'utilisateur existe déjà
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(
                "Ce nom d'utilisateur est déjà utilisé"
            );
        }

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                "Cet email est déjà utilisé"
            );
        }

        // Créer le nouvel utilisateur
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);

        return AuthResponse.registered(savedUser);
    }

    /**
     * Connexion d'un utilisateur
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Trouver l'utilisateur par email
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new InvalidCredentialsException(
                "Email ou mot de passe incorrect"
            ));

        // Vérifier le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(
                "Email ou mot de passe incorrect"
            );
        }

        // Vérifier que le compte est actif
        if (!user.isActive()) {
            throw new InvalidCredentialsException(
                "Ce compte a été désactivé"
            );
        }

        // Générer le token JWT
        String token = jwtService.generateToken(user);

        return AuthResponse.success(user, token);
    }

    /**
     * Trouver un utilisateur par ID
     */
    @Transactional(readOnly = true)
    public User findById(String id) {
        return userRepository.findById(id)
            .orElse(null);
    }
}
