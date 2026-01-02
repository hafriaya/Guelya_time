package controller;

import config.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.Film;
import model.User;
import service.FilmService;
import service.SessionService;

import java.util.List;

public class DashboardController {
    
    @FXML private TextField searchField;
    @FXML private Label usernameLabel;
    @FXML private Label sectionTitle;
    @FXML private FlowPane filmsContainer;
    @FXML private Label loadingLabel;
    @FXML private VBox contentArea;
    
    private FilmService filmService;
    
    public DashboardController() {
        this.filmService = new FilmService();
    }
    
    @FXML
    public void initialize() {
        // show current user
        User user = SessionService.getInstance().getCurrentUser();
        if (user != null) {
            usernameLabel.setText("Bonjour, " + user.getUsername());
        }
        
        // load popular films
        showPopular();
    }
    
    @FXML
    private void showHome() {
        sectionTitle.setText("Recommandations");
        loadFilms(() -> filmService.getPopularFilms(1));
    }
    
    @FXML
    private void showPopular() {
        sectionTitle.setText("Films Populaires");
        loadFilms(() -> filmService.getPopularFilms(1));
    }
    
    @FXML
    private void showWatchlist() {
        sectionTitle.setText("Ma Liste");
        User user = SessionService.getInstance().getCurrentUser();
        if (user != null) {
            loadFilms(() -> filmService.getUserWatchlist(user.getId()));
        }
    }
    
    @FXML
    private void showFavorites() {
        sectionTitle.setText("Mes Favoris");
        User user = SessionService.getInstance().getCurrentUser();
        if (user != null) {
            loadFilms(() -> filmService.getUserFavorites(user.getId()));
        }
    }
    
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            sectionTitle.setText("Résultats pour: " + query);
            loadFilms(() -> filmService.searchFilms(query, 1));
        }
    }
    
    @FXML
    private void handleLogout() {
        SessionService.getInstance().logout();
        SceneManager.getInstance().switchTo("login");
    }
    
    private void loadFilms(FilmLoader loader) {
        filmsContainer.getChildren().clear();
        loadingLabel.setVisible(true);
        
        new Thread(() -> {
            try {
                List<Film> films = loader.load();
                Platform.runLater(() -> {
                    loadingLabel.setVisible(false);
                    displayFilms(films);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingLabel.setText("Erreur de chargement");
                    loadingLabel.setVisible(true);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private void displayFilms(List<Film> films) {
        filmsContainer.getChildren().clear();
        
        if (films.isEmpty()) {
            loadingLabel.setText("Aucun film trouvé");
            loadingLabel.setVisible(true);
            return;
        }
        
        for (Film film : films) {
            VBox filmCard = createFilmCard(film);
            filmsContainer.getChildren().add(filmCard);
        }
    }
    
    private VBox createFilmCard(Film film) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #16213e; -fx-padding: 10; -fx-cursor: hand;");
        card.setPrefWidth(150);
        
        // poster image
        ImageView poster = new ImageView();
        poster.setFitWidth(130);
        poster.setFitHeight(195);
        poster.setPreserveRatio(true);
        
        String posterUrl = film.getFullPosterUrl();
        if (posterUrl != null) {
            try {
                poster.setImage(new Image(posterUrl, true));
            } catch (Exception e) {
                // use placeholder if image fails
            }
        }
        
        // title
        Label title = new Label(film.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        title.setWrapText(true);
        title.setMaxWidth(130);
        
        // rating
        Label rating = new Label("★ " + String.format("%.1f", film.getVoteAverage()));
        rating.setStyle("-fx-text-fill: #f1c40f;");
        
        card.getChildren().addAll(poster, title, rating);
        
        // click to view details
        card.setOnMouseClicked(e -> showFilmDetails(film));
        
        return card;
    }
    
    private void showFilmDetails(Film film) {
        // TODO: open film details view
        System.out.println("Clicked on: " + film.getTitle());
    }
    
    // helper interface for loading films
    @FunctionalInterface
    private interface FilmLoader {
        List<Film> load();
    }
}