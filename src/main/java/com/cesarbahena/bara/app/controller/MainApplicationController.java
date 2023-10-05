package com.cesarbahena.bara.app.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MainApplicationController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Optionally load a default view here
    }

    @FXML
    private void showMenuManagement() {
        loadView("MenuManagementView.fxml");
    }

    @FXML
    private void showRestaurantPOS() {
        loadView("RestaurantPOSView.fxml");
    }

    private void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cesarbahena/bara/app/" + fxmlFileName));
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlFileName + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
