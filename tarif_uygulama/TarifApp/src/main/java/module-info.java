module com.example.tarifapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.tarifapp to javafx.fxml;
    exports com.example.tarifapp;
    exports com.example.tarifapp.managers;
    opens com.example.tarifapp.managers to javafx.fxml;
    opens com.example.tarifapp.controllers to javafx.fxml;
    exports com.example.tarifapp.controllers;
}