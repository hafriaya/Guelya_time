package controller;

import config.SceneManager;
import service.SessionService;
import model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label userEmailLabel;
    @FXML private VBox mainContent;

    @FXML
    public void initialize() {
        loadUserInfo();
        showHomeContent();
    }

    private void loadUserInfo() {
        User currentUser = SessionService.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Bienvenue, " + currentUser.getName() + " !");
            userEmailLabel.setText(currentUser.getEmail());
        }
    }

    // ===== NAVIGATION =====
    @FXML
    private void showHome() {
        mainContent.getChildren().clear();

        // Titre
        Label title = new Label("🎬 Recommandations pour vous");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Statistiques rapides
        HBox statsBox = new HBox(20);
        statsBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        VBox stat1 = createStatCard("Films aimés", "0", "🎥");
        VBox stat2 = createStatCard("Note moyenne", "0.0", "⭐");
        VBox stat3 = createStatCard("Genres préférés", "3", "🎭");

        statsBox.getChildren().addAll(stat1, stat2, stat3);

        // Section recommandations
        Label recTitle = new Label("Films qui pourraient vous plaire");
        recTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 20 0 10 0;");

        VBox recommendations = new VBox(15);
        recommendations.getChildren().addAll(
                createFilmCard("Inception", "2010", "Sci-Fi", 4.8, "Film de science-fiction"),
                createFilmCard("The Dark Knight", "2008", "Action", 4.9, "Film de super-héros"),
                createFilmCard("Interstellar", "2014", "Sci-Fi", 4.7, "Aventure spatiale")
        );

        mainContent.getChildren().addAll(title, statsBox, recTitle, recommendations);
    }

    private VBox createStatCard(String title, String value, String icon) {
        VBox card = new VBox(5);
        card.setStyle("-fx-alignment: center; -fx-padding: 15; -fx-min-width: 120;");

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        card.getChildren().addAll(iconLabel, valueLabel, titleLabel);
        return card;
    }

    private HBox createFilmCard(String titre, String annee, String genre, double note, String description) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 15; " +
                "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");

        // Image placeholder
        VBox posterBox = new VBox();
        posterBox.setStyle("-fx-background-color: #ecf0f1; -fx-min-width: 80; -fx-min-height: 120; " +
                "-fx-background-radius: 5; -fx-alignment: center;");
        Label posterLabel = new Label("🎬");
        posterLabel.setStyle("-fx-font-size: 24px;");
        posterBox.getChildren().add(posterLabel);

        // Informations du film
        VBox infoBox = new VBox(5);

        HBox titleBox = new HBox(10);
        Label titleLabel = new Label(titre);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label yearLabel = new Label("(" + annee + ")");
        yearLabel.setStyle("-fx-text-fill: #7f8c8d;");
        titleBox.getChildren().addAll(titleLabel, yearLabel);

        Label genreLabel = new Label(genre);
        genreLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 14px;");

        HBox ratingBox = new HBox(5);
        Label ratingLabel = new Label("⭐ " + note + "/5");
        ratingLabel.setStyle("-fx-font-weight: bold;");
        ratingBox.getChildren().add(ratingLabel);

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px; -fx-wrap-text: true;");

        Button detailsBtn = new Button("Voir détails");
        detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 8 16;");

        infoBox.getChildren().addAll(titleBox, genreLabel, ratingBox, descLabel, detailsBtn);

        card.getChildren().addAll(posterBox, infoBox);
        return card;
    }

    @FXML
    private void showFilms() {
        mainContent.getChildren().clear();
        Label title = new Label("🎥 Catalogue des Films");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Barre de recherche
        HBox searchBox = new HBox(10);
        javafx.scene.control.TextField searchField = new javafx.scene.control.TextField();
        searchField.setPromptText("Rechercher un film...");
        searchField.setStyle("-fx-pref-width: 300;");

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        searchBox.getChildren().addAll(searchField, searchBtn);

        mainContent.getChildren().addAll(title, searchBox);
    }

    @FXML
    private void showSearch() {
        mainContent.getChildren().clear();
        Label title = new Label("🔍 Recherche Avancée");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox filters = new VBox(15);
        filters.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        filters.getChildren().addAll(
                createFilterSection("Genres", "Action", "Sci-Fi", "Drame", "Comédie"),
                createFilterSection("Année", "2000-2010", "2011-2020", "2021-2024"),
                createFilterSection("Note minimale", "⭐ 3+", "⭐ 4+", "⭐ 4.5+")
        );

        mainContent.getChildren().addAll(title, filters);
    }

    private VBox createFilterSection(String title, String... options) {
        VBox section = new VBox(10);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold;");

        VBox optionsBox = new VBox(5);
        for (String option : options) {
            HBox optionBox = new HBox(10);
            javafx.scene.control.CheckBox checkBox = new javafx.scene.control.CheckBox();
            Label optionLabel = new Label(option);
            optionBox.getChildren().addAll(checkBox, optionLabel);
            optionsBox.getChildren().add(optionBox);
        }

        section.getChildren().addAll(titleLabel, optionsBox);
        return section;
    }

    @FXML
    private void showProfile() {
        mainContent.getChildren().clear();

        User user = SessionService.getInstance().getCurrentUser();

        Label title = new Label("👤 Mon Profil");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox profileCard = new VBox(20);
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-padding: 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        if (user != null) {
            // Photo de profil
            VBox avatarBox = new VBox();
            avatarBox.setStyle("-fx-background-color: #3498db; -fx-min-width: 100; -fx-min-height: 100; " +
                    "-fx-background-radius: 50; -fx-alignment: center;");
            Label avatarLabel = new Label(user.getName().substring(0, 1).toUpperCase());
            avatarLabel.setStyle("-fx-font-size: 36px; -fx-text-fill: white; -fx-font-weight: bold;");
            avatarBox.getChildren().add(avatarLabel);

            // Informations
            VBox infoBox = new VBox(10);
            infoBox.getChildren().addAll(
                    createProfileField("Nom complet", user.getName()),
                    createProfileField("Email", user.getEmail()),
                    createProfileField("Date d'inscription", user.getDateInscription()),
                    createProfileField("Genres préférés", "Action, Sci-Fi, Drame")
            );

            // Boutons d'action
            HBox actionBox = new HBox(15);
            Button editBtn = new Button("✏️ Modifier le profil");
            editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10 20;");

            Button favBtn = new Button("❤️ Mes favoris");
            favBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10 20;");

            actionBox.getChildren().addAll(editBtn, favBtn);

            profileCard.getChildren().addAll(avatarBox, infoBox, actionBox);
        }

        mainContent.getChildren().addAll(title, profileCard);
    }

    private HBox createProfileField(String label, String value) {
        HBox field = new HBox(10);
        Label fieldLabel = new Label(label + ":");
        fieldLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 150;");

        Label fieldValue = new Label(value != null ? value : "Non défini");
        fieldValue.setStyle("-fx-text-fill: #2c3e50;");

        field.getChildren().addAll(fieldLabel, fieldValue);
        return field;
    }

    @FXML
    private void logout() {
        SessionService.getInstance().clearSession();
        SceneManager.getInstance().switchTo("login");
    }

    private void showHomeContent() {
        showHome();
    }
}