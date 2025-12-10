module com.guelya.guelya_time {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.neo4j.driver;


    opens com.guelya.guelya_time to javafx.fxml;
    exports com.guelya.guelya_time;
}