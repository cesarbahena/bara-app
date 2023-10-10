package com.bara.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BaraAppFX extends Application {

    @Override
    public void start(Stage stage) throws IOException {
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
