package app;

import config.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        SceneManager.getInstance().setStage(primaryStage);
        SceneManager.getInstance().switchTo("login");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
