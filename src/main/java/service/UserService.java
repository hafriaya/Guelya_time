package service;

import repository.UserRepository;
import model.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final UserRepository userRepository = new UserRepository();

    public boolean register(User user) {
        // Hash the password before saving
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        return userRepository.saveUser(user);
    }

    public boolean login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) return false;

        // Check hashed password
        return BCrypt.checkpw(password, user.getPassword());
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
