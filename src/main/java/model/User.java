package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String id ;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private List<String> favoriteGenres;

    public User(){
        this.favoriteGenres = new ArrayList<>();
    
    }
    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.password = passwordHash;
        this.createdAt = LocalDateTime.now();
        this.favoriteGenres = new ArrayList<>();
    }
    public User(String id, String username, String email, String passwordHash,
                String firstName, String lastName, LocalDateTime createdAt,
                LocalDateTime lastLogin, List<String> favoriteGenres) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.favoriteGenres = favoriteGenres != null ? favoriteGenres : new ArrayList<>();
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public List<String> getFavoriteGenres() {
        return favoriteGenres;
    }

    public void setFavoriteGenres(List<String> favoriteGenres) {
        this.favoriteGenres = favoriteGenres;
    }
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }


}
