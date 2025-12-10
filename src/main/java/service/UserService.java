package service;

import repository.UserRepository;
import model.User;

public class UserService {
    private final UserRepository userRepository = new UserRepository();

    public boolean register(User user) {
        return userRepository.saveUser(user);
    }

    public boolean login(String email, String password) {
        return userRepository.authenticateUser(email, password);
    }

    // Nouvelle méthode pour récupérer l'utilisateur complet
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}