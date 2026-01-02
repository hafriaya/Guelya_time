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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OnboardingController {
    
    @FXML private FlowPane moviesContainer;
    @FXML private Label selectionCountLabel;
    @FXML private Label loadingLabel;
    @FXML private Button submitButton;
    @FXML private Button loadMoreButton;
    
    private FilmService filmService;
    private Set<Long> selectedMovieIds = new HashSet<>();
    private int currentPage = 1;
    
    public OnboardingController() {
        this.filmService = new FilmService();
    }
    
    @FXML
    public void initialize() {
        loadMovies();
    }
    
    private void loadMovies() {
        loadingLabel.setVisible(true);
        loadMoreButton.setDisable(true);
        
        new Thread(() -> {
            try {
                List<Film> films = filmService.getPopularFilms(currentPage);
                Platform.runLater(() -> {
                    displayMovies(films);
                    loadingLabel.setVisible(false);
                    loadMoreButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingLabel.setText("Erreur de chargement");
                    loadingLabel.setVisible(true);
                    loadMoreButton.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void loadMoreMovies() {
        currentPage++;
        loadMovies();
    }
    
    private void displayMovies(List<Film> films) {
        for (Film film : films) {
            VBox movieCard = createMovieCard(film);
            moviesContainer.getChildren().add(movieCard);
        }
    }
    
    private VBox createMovieCard(Film film) {
        VBox card = new VBox(5);
        card.setPrefWidth(140);
        card.setStyle("-fx-background-color: #16213e; -fx-padding: 8; -fx-cursor: hand; -fx-background-radius: 8;");
        
        // poster
        ImageView poster = new ImageView();
        poster.setFitWidth(124);
        poster.setFitHeight(186);
        poster.setPreserveRatio(true);
        
        String posterUrl = film.getFullPosterUrl();
        if (posterUrl != null) {
            try {
                poster.setImage(new Image(posterUrl, true));
            } catch (Exception e) {
                // ignore
            }
        }
        
        // title
        Label title = new Label(film.getTitle());
        title.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
        title.setWrapText(true);
        title.setMaxWidth(124);
        
        // checkbox for selection
        CheckBox selectBox = new CheckBox("Sélectionner");
        selectBox.setStyle("-fx-text-fill: #aaa;");
        
        // handle selection
        selectBox.setOnAction(e -> {
            if (selectBox.isSelected()) {
                selectedMovieIds.add(film.getId());
                card.setStyle("-fx-background-color: #2d4a3e; -fx-padding: 8; -fx-cursor: hand; -fx-background-radius: 8; -fx-border-color: #27ae60; -fx-border-radius: 8;");
            } else {
                selectedMovieIds.remove(film.getId());
                card.setStyle("-fx-background-color: #16213e; -fx-padding: 8; -fx-cursor: hand; -fx-background-radius: 8;");
            }
            updateSelectionCount();
        });
        
        card.getChildren().addAll(poster, title, selectBox);
        
        // click on card toggles selection
        card.setOnMouseClicked(e -> {
            if (e.getTarget() != selectBox) {
                selectBox.setSelected(!selectBox.isSelected());
                selectBox.getOnAction().handle(null);
            }
        });
        
        return card;
    }
    
    private void updateSelectionCount() {
        int count = selectedMovieIds.size();
        selectionCountLabel.setText(count + " film(s) sélectionné(s)");
        
        // enable submit only if 3+ selected
        if (count >= 3) {
            submitButton.setDisable(false);
            selectionCountLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 14px;");
        } else {
            submitButton.setDisable(true);
            selectionCountLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 14px;");
        }
    }
    
    @FXML
    private void handleSubmit() {
        if (selectedMovieIds.size() < 3) {
            return;
        }
        
        submitButton.setDisable(true);
        submitButton.setText("Enregistrement...");
        
        new Thread(() -> {
            try {
                User user = SessionService.getInstance().getCurrentUser();
                
                // save selected movies to favorites
                for (Long filmId : selectedMovieIds) {
                    filmService.addToFavorites(user.getId(), filmId);
                }
                
                Platform.runLater(() -> {
                    SceneManager.getInstance().switchTo("dashboard");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    submitButton.setText("Continuer");
                });
                e.printStackTrace();
            }
        }).start();
    }
}

