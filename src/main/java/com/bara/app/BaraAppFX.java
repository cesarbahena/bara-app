package com.bara.app;

import com.bara.app.database.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BaraAppFX extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        DatabaseManager.initializeDatabase(); // Initialize the database with Flyway
        FXMLLoader fxmlLoader = new FXMLLoader(BaraAppFX.class.getResource("RestaurantPOSView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 800); // New default size for the shell UI
        stage.setTitle("BARA - Punto de Venta");
        scene.getStylesheets().add(getClass().getResource("bara-theme.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
