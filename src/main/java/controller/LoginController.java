package controller;

import java.util.Optional;

import config.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;
import service.SessionService;
import service.UserService;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;


    private UserService userService;

    public LoginController(){
        this.userService = new UserService();

    }


    //Clear error on input and Allow Enter key to submit
    @FXML
    public void initialize(){
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        

        passwordField.setOnAction(event -> handleLogin());
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    private void resetLoginButton() {
        loginButton.setDisable(false);
        loginButton.setText("Se connecter");
    }
    //handle login
    @FXML
    private void handleLogin(){
        clearError();
        String ue= usernameField.getText();
        String p= passwordField.getText();

        if (ue==null || ue.trim().isEmpty() ) {
            showError("Veuillez saisir un nom d'utilisateur ou email.");
            usernameField.requestFocus();
            return;
            
        }
        if (p==null || p.trim().isEmpty() ) {
            showError("Veuillez saisir un mot de passe");
            passwordField.requestFocus();
            return;
            
        }
        

        // Disable button during login
        loginButton.setDisable(true);
        loginButton.setText("connexion");

        // Perform login in background thread
        new Thread(() -> {
            try {
                Optional<User> userOpt = userService.login(ue.trim(), p);
                
                Platform.runLater(() -> {
                    if (userOpt.isPresent()) {
                        // Login successful
                        User user = userOpt.get();
                        SessionService.getInstance().setCurrentUser(user);
                        System.out.println("Login successful for user: " + user.getUsername());
                        
                        // Check if user completed onboarding
                        if (user.isOnboardingCompleted()) {
                            SceneManager.getInstance().switchTo("dashboard");
                        } else {
                            SceneManager.getInstance().switchTo("onboarding");
                        }
                    } else {
                        showError("Nom d'utilisateur/email ou mot de passe incorrect.");
                        passwordField.clear();
                        passwordField.requestFocus();
                        resetLoginButton();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Erreur de connexion: " + e.getMessage());
                    resetLoginButton();
                });
                e.printStackTrace();
            }
        }).start();
    }




    
}
