package com.guelya.guelya_time;

import model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class UserTest {

    @Test
    public void testUserConstructorWithUsernameEmailPassword() {
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";

        User user = new User(username, email, password);

        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getFavoriteGenres());
        assertTrue(user.getFavoriteGenres().isEmpty());
    }

    @Test
    public void testUserFullConstructor() {
        String id = "1";
        String username = "testuser";
        String email = "test@example.com";
        String password = "password123";
        String firstName = "John";
        String lastName = "Doe";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime lastLogin = LocalDateTime.now();
        List<String> favoriteGenres = Arrays.asList("Action", "Comedy");

        User user = new User(id, username, email, password, firstName, lastName, createdAt, lastLogin, favoriteGenres);

        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(lastLogin, user.getLastLogin());
        assertEquals(favoriteGenres, user.getFavoriteGenres());
    }

    @Test
    public void testDefaultConstructor() {
        User user = new User();

        assertNotNull(user.getFavoriteGenres());
        assertTrue(user.getFavoriteGenres().isEmpty());
    }

    @Test
    public void testSettersAndGetters() {
        User user = new User();

        user.setId("2");
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        user.setPassword("newpass");
        user.setFirstName("Jane");
        user.setLastName("Smith");
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());
        user.setFavoriteGenres(Arrays.asList("Drama", "Horror"));

        assertEquals("2", user.getId());
        assertEquals("newuser", user.getUsername());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("newpass", user.getPassword());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Smith", user.getLastName());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getLastLogin());
        assertEquals(Arrays.asList("Drama", "Horror"), user.getFavoriteGenres());
    }

    @Test
    public void testGetFullName() {
        // Test with both first and last name
        User user1 = new User();
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setUsername("johndoe");
        assertEquals("John Doe", user1.getFullName());

        // Test with only first name
        User user2 = new User();
        user2.setFirstName("John");
        user2.setUsername("johndoe");
        assertEquals("John", user2.getFullName());

        // Test with only last name
        User user3 = new User();
        user3.setLastName("Doe");
        user3.setUsername("johndoe");
        assertEquals("Doe", user3.getFullName());

        // Test with no names
        User user4 = new User();
        user4.setUsername("johndoe");
        assertEquals("johndoe", user4.getFullName());
    }

    @Test
    public void testToString() {
        User user = new User("1", "testuser", "test@example.com", "pass", "John", "Doe", null, null, null);
        String expected = "User{id='1', username='testuser', email='test@example.com', firstName='John', lastName='Doe'}";
        assertEquals(expected, user.toString());
    }

    @Test
    public void testFullConstructorWithNullFavoriteGenres() {
        User user = new User("1", "testuser", "test@example.com", "pass", "John", "Doe", LocalDateTime.now(), LocalDateTime.now(), null);
        assertNotNull(user.getFavoriteGenres());
        assertTrue(user.getFavoriteGenres().isEmpty());
    }
}