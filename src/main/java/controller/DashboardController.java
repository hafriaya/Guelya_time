package controller;

import service.SessionService;
import model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label userEmailLabel;
    @FXML private VBox mainContent;

    @FXML
    public void initialize() {
        loadUserInfo();
        showHome();
    }

    private void loadUserInfo() {
        User currentUser = SessionService.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getName() + " !");
            userEmailLabel.setText(currentUser.getEmail());
        }
    }

    // ===== NAVIGATION BUTTON HANDLERS =====
    @FXML
    private void showHome() {
        mainContent.getChildren().clear();
        Label label = new Label("Accueil - Contenu principal");
        mainContent.getChildren().add(label);
    }

    @FXML
    private void showFilms() {
        mainContent.getChildren().clear();
        Label label = new Label("Films - Catalogue des films");
        mainContent.getChildren().add(label);
    }

    @FXML
    private void showSearch() {
        mainContent.getChildren().clear();
        Label label = new Label("Recherche avancée");
        mainContent.getChildren().add(label);
    }

    @FXML
    private void showProfile() {
        mainContent.getChildren().clear();

        User user = SessionService.getInstance().getCurrentUser();
        Label label;
        if (user != null) {
            label = new Label("Profil de " + user.getName() + "\nEmail: " + user.getEmail() +
                    "\nInscription: " + user.getDateInscription());
        } else {
            label = new Label("Aucun utilisateur connecté.");
        }
        mainContent.getChildren().add(label);
    }

    @FXML
    private void logout() {
        SessionService.getInstance().clearSession();
        // Use your SceneManager to switch back to login
        config.SceneManager.getInstance().switchTo("login");
    }
}
