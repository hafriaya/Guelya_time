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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import model.Acteur;
import model.Film;
import service.TmdbService;

import java.util.List;

public class ActorDetailsController {
    
    @FXML private ImageView profileImage;
    @FXML private Label nameLabel;
    @FXML private Label birthdayLabel;
    @FXML private Label birthplaceLabel;
    @FXML private Label popularityLabel;
    @FXML private Label biographyLabel;
    @FXML private HBox knownForContainer;
    @FXML private Button backButton;
    
    private TmdbService tmdbService;
    private Acteur currentActeur;
    
    public ActorDetailsController() {
        this.tmdbService = new TmdbService();
    }
    
    @FXML
    public void initialize() {
        // Get actor from SceneManager
        Acteur acteur = SceneManager.getInstance().getSelectedActeur();
        if (acteur != null) {
            setActeur(acteur);
        }
    }
    
    public void setActeur(Acteur acteur) {
        this.currentActeur = acteur;
        loadActorDetails();
    }
    
    private void loadActorDetails() {
        if (currentActeur == null) return;
        
        // Show basic info immediately
        nameLabel.setText(currentActeur.getName());
        
        // Load profile image
        String profileUrl = currentActeur.getLargeProfileUrl();
        if (profileUrl != null) {
            try {
                profileImage.setImage(new Image(profileUrl, true));
            } catch (Exception e) {
                // Use placeholder
            }
        }
        
        // Load full details from API
        new Thread(() -> {
            try {
                Acteur fullDetails = tmdbService.getActorDetails(currentActeur.getId());
                List<Film> movies = tmdbService.getActorMovies(currentActeur.getId(), 12);
                
                Platform.runLater(() -> {
                    if (fullDetails != null) {
                        displayActorDetails(fullDetails);
                    }
                    displayKnownFor(movies);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void displayActorDetails(Acteur acteur) {
        // Name (might have better formatting from API)
        if (acteur.getName() != null) {
            nameLabel.setText(acteur.getName());
        }
        
        // Birthday
        if (acteur.getBirthday() != null && !acteur.getBirthday().isEmpty()) {
            birthdayLabel.setText("Born: " + acteur.getBirthday());
        } else {
            birthdayLabel.setText("");
        }
        
        // Birthplace
        if (acteur.getPlaceOfBirth() != null && !acteur.getPlaceOfBirth().isEmpty()) {
            birthplaceLabel.setText("📍 " + acteur.getPlaceOfBirth());
        } else {
            birthplaceLabel.setText("");
        }
        
        // Popularity
        popularityLabel.setText(String.format("%.1f", acteur.getPopularity()));
        
        // Biography
        if (acteur.getBiography() != null && !acteur.getBiography().isEmpty()) {
            biographyLabel.setText(acteur.getBiography());
        } else {
            biographyLabel.setText("No biography available.");
        }
        
        // Update profile image if we got a better path
        if (acteur.getProfilePath() != null) {
            String profileUrl = acteur.getLargeProfileUrl();
            if (profileUrl != null) {
                try {
                    profileImage.setImage(new Image(profileUrl, true));
                } catch (Exception e) {
                    // Keep existing
                }
            }
        }
    }
    
    private void displayKnownFor(List<Film> movies) {
        knownForContainer.getChildren().clear();
        
        for (Film film : movies) {
            VBox movieCard = createMovieCard(film);
            knownForContainer.getChildren().add(movieCard);
        }
    }
    
    private VBox createMovieCard(Film film) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(150);
        card.setStyle("-fx-background-color: #16213e; -fx-padding: 10; -fx-background-radius: 10; -fx-cursor: hand;");
        
        // Movie Poster
        ImageView posterView = new ImageView();
        posterView.setFitWidth(130);
        posterView.setFitHeight(195);
        posterView.setPreserveRatio(false);
        
        // Rounded corners for poster
        Rectangle clip = new Rectangle(130, 195);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        posterView.setClip(clip);
        
        String posterUrl = film.getFullPosterUrl();
        if (posterUrl != null) {
            try {
                posterView.setImage(new Image(posterUrl, true));
            } catch (Exception e) {
                // Use placeholder style
            }
        }
        
        // Movie Title
        Label titleLabel = new Label(film.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(130);
        titleLabel.setAlignment(Pos.CENTER);
        
        // Rating
        Label ratingLabel = new Label(String.format("★ %.1f", film.getVoteAverage()));
        ratingLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 11px;");
        
        card.getChildren().addAll(posterView, titleLabel, ratingLabel);
        
        // Click handler to show movie details
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> {
            SceneManager.getInstance().showMovieDetails(film);
        });
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #1e3057; -fx-padding: 10; -fx-background-radius: 10; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #16213e; -fx-padding: 10; -fx-background-radius: 10; -fx-cursor: hand;"));
        
        return card;
    }
    
    @FXML
    private void handleBack() {
        // Go back to movie details
        Film film = SceneManager.getInstance().getSelectedFilm();
        if (film != null) {
            SceneManager.getInstance().switchTo("movie-details");
        } else {
            SceneManager.getInstance().switchTo("dashboard");
        }
    }
}
