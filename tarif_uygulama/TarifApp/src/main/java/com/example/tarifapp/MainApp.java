package com.example.tarifapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        MainApp.primaryStage = primaryStage;
        primaryStage.setTitle("Tarif App");
        primaryStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("MainPage.fxml"))));
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }
}

// Rauf Can Özen//
