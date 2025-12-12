package controller;

import config.SceneManager;
import service.SessionService;
import service.UserService;
import model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nameField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    public void onRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String name = nameField.getText();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            messageLabel.setText("Tous les champs sont obligatoires.");
            return;
        }

        User user = new User(email, password, name);

        if (userService.register(user)) {
            SessionService.getInstance().setCurrentUser(user);
            SceneManager.getInstance().switchTo("dashboard");
        } else {
            messageLabel.setText("Cet email existe déjà.");
        }
    }

    @FXML
    public void goLogin() {
        SceneManager.getInstance().switchTo("login");
    }
}
