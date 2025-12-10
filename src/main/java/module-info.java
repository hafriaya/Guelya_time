module Guelya_time {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Required for Neo4j Java driver
    requires org.neo4j.driver;

    // Allow FXML to access controller classes
    opens controller to javafx.fxml;

    // Allow JavaFX to access application class
    opens app to javafx.graphics, javafx.fxml;

    // Export your packages (public API)
    exports app;
    exports controller;
    exports model;
    exports service;
    exports config;
}
