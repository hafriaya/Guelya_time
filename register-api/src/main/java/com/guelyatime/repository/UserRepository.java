package com.guelyatime.repository;

import com.guelyatime.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour les opérations CRUD sur les utilisateurs dans Neo4j
 */
@Repository
public interface UserRepository extends Neo4jRepository<User, String> {

    /**
     * Trouver un utilisateur par son email
     */
    Optional<User> findByEmail(String email);

    /**
     * Trouver un utilisateur par son nom d'utilisateur
     */
    Optional<User> findByUsername(String username);

    /**
     * Vérifier si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Vérifier si un nom d'utilisateur existe déjà
     */
    boolean existsByUsername(String username);

    /**
     * Trouver un utilisateur par email ou username
     */
    Optional<User> findByEmailOrUsername(String email, String username);
}
