package controller;

import config.SceneManager;
import service.SessionService;
import service.UserService;
import model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    public void onLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (userService.login(email, password)) {
            User user = userService.getUserByEmail(email);
            SessionService.getInstance().setCurrentUser(user);
            SceneManager.getInstance().switchTo("dashboard");
        } else {
            messageLabel.setText("Email ou mot de passe incorrect.");
        }
    }

    @FXML
    public void goRegister() {
        SceneManager.getInstance().switchTo("register");
    }
}
