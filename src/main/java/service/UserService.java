package service;

import model.User;
import repository.UserRepository;

public class UserService {
    private final UserRepository userRepository = new UserRepository();

    public boolean register(User user) {
        return userRepository.saveUser(user);
    }

    public boolean login(String email, String password) {
        return userRepository.authenticateUser(email, password);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
