module com.guelya.guelya_time {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.guelya.guelya_time to javafx.fxml;
    exports com.guelya.guelya_time;
}