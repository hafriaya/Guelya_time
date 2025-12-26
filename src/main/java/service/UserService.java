package service;


import model.User;
import org.mindrot.jbcrypt.BCrypt;
import repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(){
        this.userRepository = new UserRepository();
    

    }
    public UserService(UserRepository ur){
        this.userRepository= ur;
        
    }

    //input validation
    public boolean isValidUsername(String un) {
        if (un == null || un.trim().isEmpty()) {
            return false;
        }

        return un.matches("^[a-zA-Z0-9_]{3,20}$");
    }
    public boolean isValidEmail(String e) {
        if (e == null || e.trim().isEmpty()) {
            return false;
        }
        return e.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    public boolean isValidPassword(String p) {
        if (p == null || p.length() < 8) {
            return false;
        }
        boolean hasUpper = p.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = p.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = p.chars().anyMatch(Character::isDigit);
        return hasUpper && hasLower && hasDigit;
    }
    private void validateRegistrationInput(String un, String e, String p) 
            throws RegistrationException {
        if (!isValidUsername(un)) {
            throw new RegistrationException(
                "Username must be 3-20 characters long and contain only letters, numbers, and underscores");
        }
        if (!isValidEmail(e)) {
            throw new RegistrationException("Invalid email format");
        }
        if (!isValidPassword(p)) {
            throw new RegistrationException(
                "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                "one lowercase letter, and one number");
        }
    }
    

    //register new user

    public User register(String u,String e,String p) throws RegistrationException {
        validateRegistrationInput(u, e, p);

        if (userRepository.usernameExists(u)){
            throw new RegistrationException("username already exists");

        }
        if (userRepository.emailExists(e)){
            throw new RegistrationException("username already exists");

        }
        String password = BCrypt.hashpw(p, BCrypt.gensalt(12));

        User user = new User(u,e,p);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.create(user);


    }
    // public User register(String u,String e,String p,String f,String l)throws RegistrationException {
    //     User user =register(u, e, p);
    //     user.setFirstName(f);
    //     user.setLastName(l);
    //     userRepository.updateProfile(user);
    //     return user;
    // }
    
    //authentification
    public Optional<User> login(String ue, String p){
        if (ue == null || ue.trim().isEmpty() || p==null || p.isEmpty()){
            return Optional.empty();
        }
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(ue.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(p, user.getPassword())) {
                userRepository.updateLastLogin(user.getId());
                user.setLastLogin(LocalDateTime.now());
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    //change password
    public boolean changePassword(String userId, String currentPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Verify current password
        if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
            return false;
        }

        // Validate new password
        if (!isValidPassword(newPassword)) {
            return false;
        }

        // Hash and update new password
        String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        userRepository.updatePassword(userId, newPasswordHash);

        return true;
    }

    //update profile
    public void updateProfile(User user) {
        userRepository.updateProfile(user);
    }

    //get user by id
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }
    
    //get user by username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    //check if username available
    public boolean isUsernameAvailable(String username) {
        return !userRepository.usernameExists(username);
    }

    //cjeck if email available
    public boolean isEmailAvailable(String email) {
        return !userRepository.emailExists(email);
    }

    public static class RegistrationException extends Exception {
            public RegistrationException(String message) {
                super(message);
            }
        }
}