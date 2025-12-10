package config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;
    private final Map<String, String> viewPaths = new HashMap<>();

    private SceneManager() {
        viewPaths.put("login", "/fxml/login.fxml");
        viewPaths.put("register", "/fxml/register.fxml");
        viewPaths.put("dashboard", "/fxml/dashboard.fxml");
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void switchTo(String viewName) {
        try {
            String fxmlPath = viewPaths.get(viewName);
            if (fxmlPath == null) {
                throw new IllegalArgumentException("View not found: " + viewName);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            if (viewName.equals("dashboard")) {
                scene.getStylesheets().add(getClass().getResource("/css/dashboard.css").toExternalForm());
            }

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}