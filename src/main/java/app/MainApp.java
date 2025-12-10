package app;


import config.Neo4jConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Neo4jConfig.initialize();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml")
        );

        Scene scene = new Scene(loader.load());
        stage.setTitle("Guelya Time");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        Neo4jConfig.close();
    }

    public static void main(String[] args) {
        launch();
    }
}

