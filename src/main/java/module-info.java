module Guelya_time {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.neo4j.driver;

    // Ouvrir les packages au framework JavaFX
    opens controller to javafx.fxml;
    opens app to javafx.graphics, javafx.fxml;

    // Exporter les packages
    exports app;
    exports controller;
    exports model;
    exports service;
    exports config;
    exports repository;
}