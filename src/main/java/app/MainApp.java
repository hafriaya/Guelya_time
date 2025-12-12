package app;

import config.Neo4jConfig;
import config.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialiser Neo4j
        Neo4jConfig.initialize();

        // Configurer le gestionnaire de scènes
        SceneManager sceneManager = SceneManager.getInstance();
        sceneManager.setPrimaryStage(primaryStage);

        // Afficher l'écran de login
        sceneManager.switchTo("login");

        primaryStage.setTitle("Guelya Time - Recommandation de Films");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.show();
    }

    @Override
    public void stop() {
        Neo4jConfig.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
    }