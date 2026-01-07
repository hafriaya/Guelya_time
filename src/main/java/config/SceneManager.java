package config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Film;
import controller.MovieDetailsController;

import java.io.IOException;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) instance = new SceneManager();
        return instance;
    }

    public void setStage(Stage stage) { this.primaryStage = stage; }

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/movie-details.fxml"));
            Parent root = loader.load();
            
            MovieDetailsController controller = loader.getController();
            controller.setFilm(film);
            
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
