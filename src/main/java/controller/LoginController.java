package controller;

import config.SceneManager;
import service.SessionService;
import service.UserService;
import model.User;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

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
            // Récupérer l'utilisateur de la base
            User user = userService.getUserByEmail(email);

            if (user != null) {
                // Stocker dans la session
                SessionService.getInstance().setCurrentUser(user);

                // Naviguer vers le dashboard
                SceneManager.getInstance().switchTo("dashboard");
                return;
            }
        }

        messageLabel.setText("Email ou mot de passe incorrect.");
    }

    @FXML
    public void goRegister() {
        SceneManager.getInstance().switchTo("register");
    }
}