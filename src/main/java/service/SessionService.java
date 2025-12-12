package service;

import model.User;

public class SessionService {
    private static SessionService instance;
    private User currentUser;

    private SessionService() {}

    public static SessionService getInstance() {
        if (instance == null) instance = new SessionService();
        return instance;
    }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public User getCurrentUser() { return currentUser; }
    public void clearSession() { currentUser = null; }
}
