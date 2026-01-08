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
        loadFilms(false);
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
                User user = SessionService.getInstance().getCurrentUser();
                
                switch (currentSection) {
                    case "search":
                        films = filmService.searchFilms(currentSearchQuery, currentPage);
                        break;
                    case "home":
                        films = recommendationService.getPersonalizedRecommendations(user.getId(), 20 * currentPage);
                        // Skip already displayed films for pagination
                        int skip = (currentPage - 1) * 20;
                        if (skip > 0 && films.size() > skip) {
                            films = films.subList(skip, films.size());
                        } else if (skip > 0) {
                            films = List.of();
                        }
                        break;
                    case "popular":
                    default:
                        films = filmService.getPopularFilms(currentPage);
                        break;
                }
                
                List<Film> finalFilms = films;
                Platform.runLater(() -> {
                    loadingLabel.setVisible(false);
                    displayFilms(finalFilms, append);
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
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 12;");
        card.setPrefWidth(160);
        
        // poster image with container for rounded corners effect
        ImageView poster = new ImageView();
        poster.setFitWidth(140);
        poster.setFitHeight(210);
        poster.setPreserveRatio(true);
        poster.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 4);");
        
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
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;");
        title.setWrapText(true);
        title.setMaxWidth(140);
        
        // rating badge
        Label rating = new Label("★ " + String.format("%.1f", film.getVoteAverage()));
        rating.setStyle("-fx-background-color: rgba(241, 196, 15, 0.2); -fx-text-fill: #f1c40f; -fx-padding: 4 10; -fx-background-radius: 6; -fx-font-weight: bold;");
        
        card.getChildren().addAll(poster, title, rating);
        
        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #252525; -fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(123, 91, 245, 0.4), 16, 0, 0, 6);");
            card.setScaleX(1.03);
            card.setScaleY(1.03);
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 12; -fx-cursor: hand; -fx-background-radius: 12;");
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        
        // click to view details
        card.setOnMouseClicked(e -> showFilmDetails(film));
        
        return card;
    }
    
    private void showFilmDetails(Film film) {
        SceneManager.getInstance().showMovieDetails(film);
    }
    
    // helper interface for loading films
    @FunctionalInterface
    private interface FilmLoader {
        List<Film> load();
    }
}
