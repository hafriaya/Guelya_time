package controller;

import config.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import model.Film;
import model.Genre;
import model.User;
import service.FilmService;
import service.SessionService;

public class MovieDetailsController {
    
    @FXML private ImageView posterImage;
    @FXML private Label titleLabel;
    @FXML private Label ratingLabel;
    @FXML private Label voteCountLabel;
    @FXML private Label releaseDateLabel;
    @FXML private Label overviewLabel;
    @FXML private Label popularityLabel;
    @FXML private Label statusLabel;
    @FXML private FlowPane genresContainer;
    @FXML private Button watchlistButton;
    @FXML private Button favoriteButton;
    @FXML private Button backButton;
    
    private FilmService filmService;
    private Film currentFilm;
    private boolean isInWatchlist = false;
    private boolean isInFavorites = false;
    
    public MovieDetailsController() {
        this.filmService = new FilmService();
    }
    
    @FXML
    public void initialize() {
        // Film will be set via setFilm() method
    }
    
    public void setFilm(Film film) {
        this.currentFilm = film;
        displayFilmDetails();
        checkUserLists();
    }
    
    private void displayFilmDetails() {
        if (currentFilm == null) return;
        
        // Title
        titleLabel.setText(currentFilm.getTitle());
        
        // Poster
        String posterUrl = currentFilm.getFullPosterUrl();
        if (posterUrl != null) {
            try {
                posterImage.setImage(new Image(posterUrl, true));
            } catch (Exception e) {
                // Use placeholder
            }
        }
        
        // Rating
        ratingLabel.setText(String.format("★ %.1f", currentFilm.getVoteAverage()));
        voteCountLabel.setText("(" + currentFilm.getVoteCount() + " votes)");
        
        // Release Date
        String releaseDate = currentFilm.getReleaseDate();
        if (releaseDate != null && !releaseDate.isEmpty()) {
            releaseDateLabel.setText("Release: " + releaseDate);
        } else {
            releaseDateLabel.setText("Release: N/A");
        }
        
        // Overview
        String overview = currentFilm.getOverview();
        if (overview != null && !overview.isEmpty()) {
            overviewLabel.setText(overview);
        } else {
            overviewLabel.setText("No overview available.");
        }
        
        // Popularity
        popularityLabel.setText(String.format("%.0f", currentFilm.getPopularity()));
        
        // Genres
        genresContainer.getChildren().clear();
        if (currentFilm.getGenres() != null) {
            for (Genre genre : currentFilm.getGenres()) {
                Label genreTag = new Label(genre.getName());
                genreTag.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-padding: 5 12; -fx-background-radius: 15; -fx-font-size: 12px;");
                genresContainer.getChildren().add(genreTag);
            }
        }
    }
    
    private void checkUserLists() {
        User user = SessionService.getInstance().getCurrentUser();
        if (user == null || currentFilm == null) return;
        
        new Thread(() -> {
            try {
                isInWatchlist = filmService.isInWatchlist(user.getId(), currentFilm.getId());
                isInFavorites = filmService.isInFavorites(user.getId(), currentFilm.getId());
                
                Platform.runLater(() -> {
                    updateButtonStates();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void updateButtonStates() {
        if (isInWatchlist) {
            watchlistButton.setText("✓ In My List");
            watchlistButton.setStyle("-fx-background-color: #1e5631; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand;");
        } else {
            watchlistButton.setText("+ Add to My List");
            watchlistButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand;");
        }
        
        if (isInFavorites) {
            favoriteButton.setText("♥ In Favorites");
            favoriteButton.setStyle("-fx-background-color: #8b1538; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand;");
        } else {
            favoriteButton.setText("♥ Add to Favorites");
            favoriteButton.setStyle("-fx-background-color: #e94560; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand;");
        }
    }
    
    @FXML
    private void handleWatchlist() {
        User user = SessionService.getInstance().getCurrentUser();
        if (user == null || currentFilm == null) return;
        
        watchlistButton.setDisable(true);
        
        new Thread(() -> {
            try {
                if (isInWatchlist) {
                    filmService.removeFromWatchlist(user.getId(), currentFilm.getId());
                    isInWatchlist = false;
                    showStatus("Removed from My List");
                } else {
                    filmService.addToWatchlist(user.getId(), currentFilm.getId());
                    isInWatchlist = true;
                    showStatus("Added to My List!");
                }
                
                Platform.runLater(() -> {
                    updateButtonStates();
                    watchlistButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showStatus("Error: " + e.getMessage());
                    watchlistButton.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void handleFavorite() {
        User user = SessionService.getInstance().getCurrentUser();
        if (user == null || currentFilm == null) return;
        
        favoriteButton.setDisable(true);
        
        new Thread(() -> {
            try {
                if (isInFavorites) {
                    filmService.removeFromFavorites(user.getId(), currentFilm.getId());
                    isInFavorites = false;
                    showStatus("Removed from Favorites");
                } else {
                    filmService.addToFavorites(user.getId(), currentFilm.getId());
                    isInFavorites = true;
                    showStatus("Added to Favorites!");
                }
                
                Platform.runLater(() -> {
                    updateButtonStates();
                    favoriteButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showStatus("Error: " + e.getMessage());
                    favoriteButton.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private void showStatus(String message) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setVisible(true);
            
            // Hide after 3 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> statusLabel.setVisible(false));
                } catch (InterruptedException e) {
                    // ignore
                }
            }).start();
        });
    }
    
    @FXML
    private void handleBack() {
        SceneManager.getInstance().switchTo("dashboard");
    }
}
