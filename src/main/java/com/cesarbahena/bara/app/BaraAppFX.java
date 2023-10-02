package com.cesarbahena.bara.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BaraAppFX extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BaraAppFX.class.getResource("MainApplicationView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800); // Increased size for main application
        stage.setTitle("BARA - TPV Restaurante");
        scene.getStylesheets().add(getClass().getResource("bara-theme.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
