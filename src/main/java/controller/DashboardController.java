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
import service.RecommendationService;
import service.SessionService;

import java.util.List;

public class DashboardController {
    
    @FXML private TextField searchField;
    @FXML private Label usernameLabel;
    @FXML private Label sectionTitle;
    @FXML private FlowPane filmsContainer;
    @FXML private Label loadingLabel;
    @FXML private VBox contentArea;
    @FXML private Button showMoreButton;
    
    private FilmService filmService;
    private RecommendationService recommendationService;
    private int currentPage = 1;
    private String currentSection = "popular";
    private String currentSearchQuery = "";
    
    public DashboardController() {
        this.filmService = new FilmService();
        this.recommendationService = new RecommendationService();
    }
    
    @FXML
    public void initialize() {
        // show current user
        User user = SessionService.getInstance().getCurrentUser();
        if (user != null) {
            usernameLabel.setText("Hello, " + user.getUsername());
        }
        
        // load popular films
        showPopular();
    }
    
    @FXML
    private void showHome() {
        currentSection = "home";
        currentPage = 1;
        sectionTitle.setText("Recommendations for You");
        filmsContainer.getChildren().clear();
        showMoreButton.setVisible(false);
        
        loadingLabel.setVisible(true);
        
        new Thread(() -> {
            try {
                User user = SessionService.getInstance().getCurrentUser();
                List<Film> recommendations = recommendationService.getPersonalizedRecommendations(user.getId(), 20);
                
                Platform.runLater(() -> {
                    loadingLabel.setVisible(false);
                    displayFilms(recommendations, false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingLabel.setText("Error loading recommendations");
                    loadingLabel.setVisible(true);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void showPopular() {
        currentSection = "popular";
        currentPage = 1;
        sectionTitle.setText("Popular Movies");
        filmsContainer.getChildren().clear();
        loadFilms(false);
    }
    
    @FXML
    private void showWatchlist() {
        currentSection = "watchlist";
        currentPage = 1;
        sectionTitle.setText("My List");
        filmsContainer.getChildren().clear();
        showMoreButton.setVisible(false);
        
        User user = SessionService.getInstance().getCurrentUser();
        if (user != null) {
            loadUserList(() -> filmService.getUserWatchlist(user.getId()));
        }
    }
    
    @FXML
    private void showFavorites() {
        currentSection = "favorites";
        currentPage = 1;
        sectionTitle.setText("Favorites");
        filmsContainer.getChildren().clear();
        showMoreButton.setVisible(false);
        
        User user = SessionService.getInstance().getCurrentUser();
        if (user != null) {
            loadUserList(() -> filmService.getUserFavorites(user.getId()));
        }
    }
    
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            currentSection = "search";
            currentSearchQuery = query;
            currentPage = 1;
            sectionTitle.setText("Results for: " + query);
            filmsContainer.getChildren().clear();
            loadFilms(false);
        }
    }
    
    @FXML
    private void loadMoreMovies() {
        currentPage++;
        loadFilms(true);
    }
    
    @FXML
    private void handleLogout() {
        SessionService.getInstance().logout();
        SceneManager.getInstance().switchTo("login");
    }
    
    private void loadFilms(boolean append) {
        loadingLabel.setVisible(true);
        showMoreButton.setDisable(true);
        
        new Thread(() -> {
            try {
                List<Film> films;
                
                switch (currentSection) {
                    case "search":
                        films = filmService.searchFilms(currentSearchQuery, currentPage);
                        break;
                    case "home":
                    case "popular":
                    default:
                        films = filmService.getPopularFilms(currentPage);
                        break;
                }
                
                Platform.runLater(() -> {
                    loadingLabel.setVisible(false);
                    displayFilms(films, append);
                    showMoreButton.setVisible(true);
                    showMoreButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingLabel.setText("Error loading movies");
                    loadingLabel.setVisible(true);
                    showMoreButton.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private void loadUserList(FilmLoader loader) {
        loadingLabel.setVisible(true);
        
        new Thread(() -> {
            try {
                List<Film> films = loader.load();
                Platform.runLater(() -> {
                    loadingLabel.setVisible(false);
                    displayFilms(films, false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingLabel.setText("Error loading movies");
                    loadingLabel.setVisible(true);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private void displayFilms(List<Film> films, boolean append) {
        if (!append) {
            filmsContainer.getChildren().clear();
        }
        
        if (films.isEmpty() && filmsContainer.getChildren().isEmpty()) {
            loadingLabel.setText("No movies found");
            loadingLabel.setVisible(true);
            showMoreButton.setVisible(false);
            return;
        }
        
        for (Film film : films) {
            VBox filmCard = createFilmCard(film);
            filmsContainer.getChildren().add(filmCard);
        }
    }
    
    private VBox createFilmCard(Film film) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #16213e; -fx-padding: 10; -fx-cursor: hand; -fx-background-radius: 8;");
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
