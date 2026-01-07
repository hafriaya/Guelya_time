package config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Acteur;
import model.Film;

import java.io.IOException;

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
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlName + ".fxml"));
            primaryStage.setScene(new Scene(root));
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
