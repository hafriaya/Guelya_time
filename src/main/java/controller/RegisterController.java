package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import service.UserService;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField nameField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    public void onRegister() {
        User user = new User(
                emailField.getText(),
                passwordField.getText(),
                nameField.getText()
        );

        if (userService.register(user)) {
            messageLabel.setText("Registration successful!");
        } else {
            messageLabel.setText("Failed to register.");
        }
    }

    @FXML
    public void goLogin() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/login.fxml")));
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }
}