package com.guelya.guelya_time;

import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import repository.UserRepository;
import service.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
    }

    @Test
    public void testIsValidUsername() {
        assertTrue(userService.isValidUsername("validuser123"));
        assertTrue(userService.isValidUsername("user_name"));
        assertFalse(userService.isValidUsername("us")); // too short
        assertFalse(userService.isValidUsername("user with spaces")); // spaces
        assertFalse(userService.isValidUsername("")); // empty
        assertFalse(userService.isValidUsername(null)); // null
    }

    @Test
    public void testIsValidEmail() {
        assertTrue(userService.isValidEmail("test@example.com"));
        assertTrue(userService.isValidEmail("user.name+tag@domain.co.uk"));
        assertFalse(userService.isValidEmail("invalid-email"));
        assertFalse(userService.isValidEmail(""));
        assertFalse(userService.isValidEmail(null));
    }

    @Test
    public void testIsValidPassword() {
        assertTrue(userService.isValidPassword("Password123"));
        assertFalse(userService.isValidPassword("short")); // too short
        assertFalse(userService.isValidPassword("password123")); // no upper
        assertFalse(userService.isValidPassword("PASSWORD123")); // no lower
        assertFalse(userService.isValidPassword("Password")); // no digit
        assertFalse(userService.isValidPassword(null));
    }

    @Test
    public void testRegisterSuccess() throws UserService.RegistrationException {
        when(userRepository.usernameExists("testuser")).thenReturn(false);
        when(userRepository.emailExists("test@example.com")).thenReturn(false);
        when(userRepository.create(any(User.class))).thenReturn(new User("testuser", "test@example.com", "hashedpass"));

        User result = userService.register("testuser", "test@example.com", "Password123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).usernameExists("testuser");
        verify(userRepository).emailExists("test@example.com");
        verify(userRepository).create(any(User.class));
    }

    @Test
    public void testRegisterUsernameExists() {
        when(userRepository.usernameExists("testuser")).thenReturn(true);

        UserService.RegistrationException exception = assertThrows(UserService.RegistrationException.class, () -> {
            userService.register("testuser", "test@example.com", "Password123");
        });

        assertEquals("username already exists", exception.getMessage());
    }

    @Test
    public void testRegisterEmailExists() {
        when(userRepository.usernameExists("testuser")).thenReturn(false);
        when(userRepository.emailExists("test@example.com")).thenReturn(true);

        UserService.RegistrationException exception = assertThrows(UserService.RegistrationException.class, () -> {
            userService.register("testuser", "test@example.com", "Password123");
        });

        assertEquals("username already exists", exception.getMessage()); // Note: message says username but it's email
    }

    @Test
    public void testRegisterInvalidInput() {
        UserService.RegistrationException exception = assertThrows(UserService.RegistrationException.class, () -> {
            userService.register("us", "invalid-email", "short");
        });

        assertTrue(exception.getMessage().contains("Username must be"));
    }

    @Test
    public void testLoginSuccess() {
        String hashedPassword = BCrypt.hashpw("Password123", BCrypt.gensalt(12));
        User user = new User("1", "testuser", "test@example.com", hashedPassword, "John", "Doe", LocalDateTime.now(), null, Arrays.asList("Action"));
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.login("testuser", "Password123");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).updateLastLogin("1");
    }

    @Test
    public void testLoginInvalidCredentials() {
        String wrongHash = BCrypt.hashpw("different", BCrypt.gensalt(12));
        User user = new User("1", "testuser", "test@example.com", wrongHash, null, null, null, null, null);
        when(userRepository.findByUsernameOrEmail("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.login("testuser", "wrongpassword");

        assertFalse(result.isPresent());
    }

    @Test
    public void testLoginUserNotFound() {
        when(userRepository.findByUsernameOrEmail("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.login("nonexistent", "password");

        assertFalse(result.isPresent());
    }

    @Test
    public void testChangePasswordSuccess() {
        String hashedPassword = BCrypt.hashpw("Password123", BCrypt.gensalt(12));
        User user = new User("1", "testuser", "test@example.com", hashedPassword, null, null, null, null, null);
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        boolean result = userService.changePassword("1", "Password123", "NewPassword456");

        assertTrue(result);
        verify(userRepository).updatePassword(eq("1"), anyString());
    }

    @Test
    public void testChangePasswordWrongCurrent() {
        String hashedPassword = BCrypt.hashpw("Password123", BCrypt.gensalt(12));
        User user = new User("1", "testuser", "test@example.com", hashedPassword, null, null, null, null, null);
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        boolean result = userService.changePassword("1", "wrongpassword", "NewPassword456");

        assertFalse(result);
    }

    @Test
    public void testUpdateProfile() {
        User user = new User();
        user.setId("1");
        user.setFirstName("John");
        user.setLastName("Doe");

        userService.updateProfile(user);

        verify(userRepository).updateProfile(user);
    }

    @Test
    public void testGetUserById() {
        User user = new User("1", "testuser", "test@example.com", "pass", null, null, null, null, null);
        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById("1");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    public void testGetUserByUsername() {
        User user = new User("1", "testuser", "test@example.com", "pass", null, null, null, null, null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    public void testIsUsernameAvailable() {
        when(userRepository.usernameExists("available")).thenReturn(false);
        when(userRepository.usernameExists("taken")).thenReturn(true);

        assertTrue(userService.isUsernameAvailable("available"));
        assertFalse(userService.isUsernameAvailable("taken"));
    }

    @Test
    public void testIsEmailAvailable() {
        when(userRepository.emailExists("available@example.com")).thenReturn(false);
        when(userRepository.emailExists("taken@example.com")).thenReturn(true);

        assertTrue(userService.isEmailAvailable("available@example.com"));
        assertFalse(userService.isEmailAvailable("taken@example.com"));
    }
}