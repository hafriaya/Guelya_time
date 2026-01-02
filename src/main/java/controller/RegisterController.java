package controller;

import config.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.UserService;

public class RegisterController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink loginLink;
    
    private UserService userService;
    
    public RegisterController() {
        this.userService = new UserService();
    }
    
    @FXML
    public void initialize() {
        // clear error when user types
        usernameField.textProperty().addListener((obs, old, val) -> clearError());
        emailField.textProperty().addListener((obs, old, val) -> clearError());
        passwordField.textProperty().addListener((obs, old, val) -> clearError());
        confirmPasswordField.textProperty().addListener((obs, old, val) -> clearError());
    }
    
    @FXML
    private void handleRegister() {
        clearError();
        
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // validation
        if (username.isEmpty()) {
            showError("Le nom d'utilisateur est requis");
            return;
        }
        if (email.isEmpty()) {
            showError("L'email est requis");
            return;
        }
        if (!email.contains("@")) {
            showError("Email invalide");
            return;
        }
        if (password.isEmpty()) {
            showError("Le mot de passe est requis");
            return;
        }
        if (password.length() < 6) {
            showError("Le mot de passe doit avoir au moins 6 caractères");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }
        
        try {
            userService.register(username, email, password);
            System.out.println("Registration successful for: " + username);
            // go to login page
            SceneManager.getInstance().switchTo("login");
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLoginLink() {
        SceneManager.getInstance().switchTo("login");
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
}