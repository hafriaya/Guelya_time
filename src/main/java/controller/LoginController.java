package controller;


import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.UserService;


public class LoginController {

    @FXML
    private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    public void onLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (userService.login(email, password)) {
            loadHome();
        } else {
            messageLabel.setText("Invalid email or password.");
        }
    }

    @FXML
    public void goRegister() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/register.fxml")));
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadHome() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/fxml/home.fxml")));
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }
}