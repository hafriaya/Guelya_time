package config;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import model.Acteur;
import model.Film;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;
    private Film selectedFilm;
    private Acteur selectedActeur;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) instance = new SceneManager();
        return instance;
    }

    public void setStage(Stage stage) { this.primaryStage = stage; }
    
    public Stage getStage() { return primaryStage; }
    
    public void setSelectedFilm(Film film) { this.selectedFilm = film; }
    
    public Film getSelectedFilm() { return selectedFilm; }
    
    public void setSelectedActeur(Acteur acteur) { this.selectedActeur = acteur; }
    
    public Acteur getSelectedActeur() { return selectedActeur; }

    public void switchTo(String fxmlName) {
        try {
            // Preserve fullscreen/maximized state
            boolean wasFullScreen = primaryStage != null && primaryStage.isFullScreen();
            boolean wasMaximized = primaryStage != null && primaryStage.isMaximized();

            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlName + ".fxml"));

            // If a Scene already exists, reuse it to avoid losing fullscreen/window state.
            if (primaryStage.getScene() == null) {
                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
            } else {
                // Replace the root of the existing scene
                primaryStage.getScene().setRoot(root);
            }

            // If root is a Region (most layouts), bind its preferred size to the stage size
            if (root instanceof Region) {
                Region region = (Region) root;
                // Unbind previous bindings first (safe if none)
                try {
                    region.prefWidthProperty().unbind();
                    region.prefHeightProperty().unbind();
                } catch (Exception ignored) {}

                region.prefWidthProperty().bind(primaryStage.widthProperty());
                region.prefHeightProperty().bind(primaryStage.heightProperty());
            }

            // Restore fullscreen/maximized state after setting the new scene
            if (wasFullScreen) {
                primaryStage.setFullScreen(true);
            } else if (wasMaximized) {
                primaryStage.setMaximized(true);
            }

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showMovieDetails(Film film) {
        setSelectedFilm(film);
        switchTo("movie-details");
    }
    
    public void showActorDetails(Acteur acteur) {
        setSelectedActeur(acteur);
        switchTo("actor-details");
    }
}
