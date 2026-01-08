package controller;

import config.SceneManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import model.Acteur;
import model.Film;
import model.Genre;
import model.User;
import service.FilmService;
import service.SessionService;
import service.TmdbService;

import java.util.List;

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
    @FXML private HBox castContainer;
    @FXML private HBox directorBox;
    @FXML private HBox directorContainer;
    @FXML private ImageView directorPhoto;
    @FXML private Label directorLabel;
    @FXML private VBox directorFilmsSection;
    @FXML private Label directorFilmsLabel;
    @FXML private HBox directorFilmsContainer;
    @FXML private Button watchlistButton;
    @FXML private Button favoriteButton;
    @FXML private Button backButton;
    
    private FilmService filmService;
    private TmdbService tmdbService;
    private Film currentFilm;
    private Acteur currentDirector;
    private boolean isInWatchlist = false;
    private boolean isInFavorites = false;
    
    public MovieDetailsController() {
        this.filmService = new FilmService();
        this.tmdbService = new TmdbService();
    }
    
    @FXML
    public void initialize() {
        // Get film from SceneManager
        Film film = SceneManager.getInstance().getSelectedFilm();
        if (film != null) {
            setFilm(film);
        }
    }
    
    public void setFilm(Film film) {
        this.currentFilm = film;
        displayFilmDetails();
        checkUserLists();
        loadCast();
        loadDirector();
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
                genreTag.setStyle("-fx-background-color: rgba(123, 91, 245, 0.2); -fx-text-fill: #a890ff; -fx-padding: 6 14; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;");
                genreTag.setOnMouseEntered(e -> genreTag.setStyle("-fx-background-color: rgba(123, 91, 245, 0.4); -fx-text-fill: #ffffff; -fx-padding: 6 14; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;"));
                genreTag.setOnMouseExited(e -> genreTag.setStyle("-fx-background-color: rgba(123, 91, 245, 0.2); -fx-text-fill: #a890ff; -fx-padding: 6 14; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;"));
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
            watchlistButton.setStyle("-fx-background-color: #1e5631; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 8;");
        } else {
            watchlistButton.setText("+ Add to My List");
            watchlistButton.setStyle("-fx-background-color: #7b5bf5; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 8;");
        }
        
        if (isInFavorites) {
            favoriteButton.setText("♥ In Favorites");
            favoriteButton.setStyle("-fx-background-color: #8b1538; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 8;");
        } else {
            favoriteButton.setText("♥ Add to Favorites");
            favoriteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20; -fx-cursor: hand; -fx-background-radius: 8;");
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
    
    private void loadCast() {
        if (currentFilm == null) return;
        
        new Thread(() -> {
            try {
                List<Acteur> cast = tmdbService.getMovieCast(currentFilm.getId(), 10);
                
                Platform.runLater(() -> {
                    castContainer.getChildren().clear();
                    
                    for (Acteur acteur : cast) {
                        VBox actorCard = createActorCard(acteur);
                        castContainer.getChildren().add(actorCard);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private VBox createActorCard(Acteur acteur) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(130);
        card.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 12; -fx-background-radius: 12; -fx-cursor: hand;");
        
        // Actor Photo
        ImageView photoView = new ImageView();
        photoView.setFitWidth(105);
        photoView.setFitHeight(130);
        photoView.setPreserveRatio(false);
        
        // Rounded corners for photo
        Rectangle clip = new Rectangle(105, 130);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        photoView.setClip(clip);
        
        String profileUrl = acteur.getFullProfileUrl();
        if (profileUrl != null) {
            try {
                photoView.setImage(new Image(profileUrl, true));
            } catch (Exception e) {
                // Use placeholder style
                photoView.setStyle("-fx-background-color: #252525;");
            }
        }
        
        // Actor Name
        Label nameLabel = new Label(acteur.getName());
        nameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-font-weight: bold;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(105);
        nameLabel.setAlignment(Pos.CENTER);
        
        // Character Name
        Label characterLabel = new Label(acteur.getCharacter() != null ? acteur.getCharacter() : "");
        characterLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        characterLabel.setWrapText(true);
        characterLabel.setMaxWidth(105);
        characterLabel.setAlignment(Pos.CENTER);
        
        card.getChildren().addAll(photoView, nameLabel, characterLabel);
        
        // Click handler to show actor details
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> showActorDetails(acteur));
        
        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #252525; -fx-padding: 12; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(123, 91, 245, 0.3), 12, 0, 0, 4);");
            card.setScaleX(1.03);
            card.setScaleY(1.03);
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 12; -fx-background-radius: 12; -fx-cursor: hand;");
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        
        return card;
    }
    
    private void showActorDetails(Acteur acteur) {
        SceneManager.getInstance().setSelectedActeur(acteur);
        SceneManager.getInstance().switchTo("actor-details");
    }
    
    private void loadDirector() {
        if (currentFilm == null) return;
        
        new Thread(() -> {
            try {
                Acteur director = tmdbService.getMovieDirector(currentFilm.getId());
                
                if (director != null) {
                    currentDirector = director;
                    List<Film> directorFilms = tmdbService.getDirectorMovies(director.getId(), 10);
                    
                    Platform.runLater(() -> {
                        displayDirector(director);
                        displayDirectorFilms(directorFilms);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void displayDirector(Acteur director) {
        // Set director name
        directorLabel.setText(director.getName());
        
        // Set director photo with rounded corners
        String profileUrl = director.getFullProfileUrl();
        if (profileUrl != null) {
            try {
                directorPhoto.setImage(new Image(profileUrl, true));
                Rectangle clip = new Rectangle(40, 40);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                directorPhoto.setClip(clip);
            } catch (Exception e) {
                directorPhoto.setVisible(false);
            }
        } else {
            directorPhoto.setVisible(false);
        }
        
        // Make director clickable
        directorContainer.setCursor(Cursor.HAND);
        directorContainer.setOnMouseClicked(e -> showDirectorDetails(director));
        
        // Hover effect on director name
        directorLabel.setOnMouseEntered(e -> directorLabel.setStyle("-fx-text-fill: #a890ff; -fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand; -fx-underline: true;"));
        directorLabel.setOnMouseExited(e -> directorLabel.setStyle("-fx-text-fill: #7b5bf5; -fx-font-size: 15px; -fx-font-weight: bold; -fx-cursor: hand;"));
    }
    
    private void displayDirectorFilms(List<Film> films) {
        if (films == null || films.isEmpty()) return;
        
        // Filter out current movie
        films.removeIf(f -> f.getId() == currentFilm.getId());
        
        if (films.isEmpty()) return;
        
        // Show section
        directorFilmsSection.setVisible(true);
        directorFilmsSection.setManaged(true);
        
        // Update label with director name
        if (currentDirector != null) {
            directorFilmsLabel.setText("More from " + currentDirector.getName());
        }
        
        // Add film cards
        directorFilmsContainer.getChildren().clear();
        for (Film film : films) {
            VBox filmCard = createFilmCard(film);
            directorFilmsContainer.getChildren().add(filmCard);
        }
    }
    
    private VBox createFilmCard(Film film) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(150);
        card.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 12; -fx-background-radius: 12; -fx-cursor: hand;");
        
        // Poster
        ImageView posterView = new ImageView();
        posterView.setFitWidth(130);
        posterView.setFitHeight(195);
        posterView.setPreserveRatio(false);
        
        // Rounded corners
        Rectangle clip = new Rectangle(130, 195);
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        posterView.setClip(clip);
        
        String posterUrl = film.getFullPosterUrl();
        if (posterUrl != null) {
            try {
                posterView.setImage(new Image(posterUrl, true));
            } catch (Exception e) {
                // placeholder
            }
        }
        
        // Title
        Label titleLbl = new Label(film.getTitle());
        titleLbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-font-weight: bold;");
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(130);
        titleLbl.setAlignment(Pos.CENTER);
        
        // Rating badge
        Label ratingLbl = new Label(String.format("★ %.1f", film.getVoteAverage()));
        ratingLbl.setStyle("-fx-background-color: rgba(241, 196, 15, 0.2); -fx-text-fill: #f1c40f; -fx-padding: 3 8; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(posterView, titleLbl, ratingLbl);
        
        // Click to view
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> {
            SceneManager.getInstance().showMovieDetails(film);
        });
        
        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #252525; -fx-padding: 12; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(123, 91, 245, 0.3), 12, 0, 0, 4);");
            card.setScaleX(1.03);
            card.setScaleY(1.03);
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 12; -fx-background-radius: 12; -fx-cursor: hand;");
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
        
        return card;
    }
    
    private void showDirectorDetails(Acteur director) {
        SceneManager.getInstance().setSelectedActeur(director);
        SceneManager.getInstance().switchTo("actor-details");
    }
    
    @FXML
    private void handleBack() {
        SceneManager.getInstance().switchTo("dashboard");
    }
}
