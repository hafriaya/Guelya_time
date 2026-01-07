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
import service.UserService;
import repository.GenreRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OnboardingController {
    
    @FXML private FlowPane moviesContainer;
    @FXML private Label selectionCountLabel;
    @FXML private Label loadingLabel;
    @FXML private Button submitButton;
    @FXML private Button loadMoreButton;
    
    private FilmService filmService;
    private UserService userService;
    private GenreRepository genreRepository;
    private Set<Long> selectedMovieIds = new HashSet<>();
    private int currentPage = 1;
    
    public OnboardingController() {
        this.filmService = new FilmService();
        this.userService = new UserService();
        this.genreRepository = new GenreRepository();
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
                
                // Save selected movies to favorites and extract genres
                Set<Integer> allGenreIds = new HashSet<>();
                for (Long filmId : selectedMovieIds) {
                    // Add to favorites
                    filmService.addToFavorites(user.getId(), filmId);
                    
                    // Get film and extract genres
                    filmService.getFilmById(filmId).ifPresent(film -> {
                        if (film.getGenres() != null) {
                            allGenreIds.addAll(
                                film.getGenres().stream()
                                    .map(g -> g.getId())
                                    .collect(Collectors.toList())
                            );
                        }
                    });
                }
                
                // Save extracted genres to user preferences
                if (!allGenreIds.isEmpty()) {
                    genreRepository.setUserFavoriteGenres(user.getId(), new java.util.ArrayList<>(allGenreIds));
                }
                
                // Mark onboarding as completed
                userService.completeOnboarding(user.getId());
                user.setOnboardingCompleted(true);
                SessionService.getInstance().setCurrentUser(user);
        }).start();
    }
}

