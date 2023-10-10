package com.bara.app.controller;

import com.bara.app.model.MenuItem;
import com.bara.app.repository.MenuItemRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class MenuManagementController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField priceField;
    @FXML
    private TextArea descriptionArea;

    @FXML
    private TableView<MenuItem> menuItemTable;
    @FXML
    private TableColumn<MenuItem, Integer> idColumn;
    @FXML
    private TableColumn<MenuItem, String> nameColumn;
    @FXML
    private TableColumn<MenuItem, Double> priceColumn;
    @FXML
    private TableColumn<MenuItem, String> descriptionColumn;

    private MenuItemRepository menuItemRepository;
    private ObservableList<MenuItem> menuItemList;

    @FXML
    public void initialize() {
        menuItemRepository = new MenuItemRepository();
        menuItemList = FXCollections.observableArrayList();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        menuItemTable.setItems(menuItemList);
        loadMenuItems();

        menuItemTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showMenuItemDetails(newValue));
    }

    private void loadMenuItems() {
        menuItemList.setAll(menuItemRepository.findAll());
    }

    private void showMenuItemDetails(MenuItem menuItem) {
        if (menuItem != null) {
            nameField.setText(menuItem.getName());
            priceField.setText(String.valueOf(menuItem.getPrice()));
            descriptionArea.setText(menuItem.getDescription());
        } else {
            clearFields();
        }
    }

    @FXML
    private void addMenuItem() {
        try {
            String name = nameField.getText();
            double price = Double.parseDouble(priceField.getText());
            String description = descriptionArea.getText();

            if (name.isEmpty()) {
                showAlert("Error", "El nombre del item no puede estar vacío.");
                return;
            }

            MenuItem menuItem = new MenuItem(name, price, description);
            menuItemRepository.save(menuItem);
            loadMenuItems();
            clearFields();
        } catch (NumberFormatException e) {
            showAlert("Error", "Por favor, ingrese un precio válido.");
        }
    }

    @FXML
    private void updateMenuItem() {
        try {
            MenuItem selectedMenuItem = menuItemTable.getSelectionModel().getSelectedItem();
            if (selectedMenuItem != null) {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                String description = descriptionArea.getText();

                if (name.isEmpty()) {
                    showAlert("Error", "El nombre del item no puede estar vacío.");
                    return;
                }

                selectedMenuItem.setName(name);
                selectedMenuItem.setPrice(price);
                selectedMenuItem.setDescription(description);

                menuItemRepository.update(selectedMenuItem);
                loadMenuItems();
                clearFields();
            } else {
                showAlert("Advertencia", "Seleccione un item para actualizar.");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Por favor, ingrese un precio válido.");
        }
    }

    @FXML
    private void deleteMenuItem() {
        MenuItem selectedMenuItem = menuItemTable.getSelectionModel().getSelectedItem();
        if (selectedMenuItem != null) {
            menuItemRepository.delete(selectedMenuItem.getId());
            loadMenuItems();
            clearFields();
        } else {
            showAlert("Advertencia", "Seleccione un item para eliminar.");
        }
    }

    @FXML
    private void clearFields() {
        nameField.clear();
        priceField.clear();
        descriptionArea.clear();
        menuItemTable.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
