package com.cesarbahena.bara.app.controller;

import com.cesarbahena.bara.app.model.MenuItem;
import com.cesarbahena.bara.app.model.OrderItem;
import com.cesarbahena.bara.app.repository.MenuItemRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.TextAlignment;

import java.util.Optional;

public class RestaurantPOSController {

    @FXML
    private FlowPane menuFlowPane;
    @FXML
    private TableView<OrderItem> orderTable;
    @FXML
    private TableColumn<OrderItem, String> orderItemNameColumn;
    @FXML
    private TableColumn<OrderItem, Integer> orderItemQuantityColumn;
    @FXML
    private TableColumn<OrderItem, Double> orderItemPriceColumn;
    @FXML
    private TableColumn<OrderItem, Double> orderItemTotalPriceColumn;
    @FXML
    private TextArea itemNotesArea;
    @FXML
    private Label totalLabel;

    private MenuItemRepository menuItemRepository;
    private ObservableList<OrderItem> currentOrder;

    @FXML
    public void initialize() {
        menuItemRepository = new MenuItemRepository();
        currentOrder = FXCollections.observableArrayList();

        setupTable();
        loadMenuItems();

        orderTable.setItems(currentOrder);
        orderTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> showItemNotes(newSelection));
    }

    private void setupTable() {
        orderItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("menuItemName"));
        orderItemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        orderItemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("menuItemPrice"));
        orderItemTotalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
    }

    private void loadMenuItems() {
        menuFlowPane.getChildren().clear();
        for (MenuItem item : menuItemRepository.findAll()) {
            Button itemButton = createMenuItemButton(item);
            menuFlowPane.getChildren().add(itemButton);
        }
    }

    private Button createMenuItemButton(MenuItem item) {
        Button button = new Button(item.getName() + "\n" + String.format("$%.2f", item.getPrice()));
        button.getStyleClass().add("menu-item-button");
        button.setPrefSize(120, 80);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setOnAction(event -> addOrderItem(item));
        return button;
    }

    private void addOrderItem(MenuItem menuItem) {
        Optional<OrderItem> existingItem = currentOrder.stream()
                .filter(orderItem -> orderItem.getMenuItem().getId() == menuItem.getId())
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
            orderTable.refresh();
        } else {
            currentOrder.add(new OrderItem(menuItem, 1));
        }
        updateTotal();
    }

    private void showItemNotes(OrderItem item) {
        if (item != null) {
            itemNotesArea.setText(item.getNotes());
        } else {
            itemNotesArea.clear();
        }
    }

    @FXML
    private void adjustQuantity() {
        OrderItem selectedItem = orderTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedItem.getQuantity()));
            dialog.setTitle("Ajustar Cantidad");
            dialog.setHeaderText("Ajustar cantidad para: " + selectedItem.getMenuItemName());
            dialog.setContentText("Nueva cantidad:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(quantityStr -> {
                try {
                    int newQuantity = Integer.parseInt(quantityStr);
                    if (newQuantity > 0) {
                        selectedItem.setQuantity(newQuantity);
                        orderTable.refresh();
                        updateTotal();
                    } else {
                        showAlert("Error", "La cantidad debe ser mayor a cero.");
                    }
                } catch (NumberFormatException e) {
                    showAlert("Error", "Por favor, ingrese un número válido.");
                }
            });
        } else {
            showAlert("Advertencia", "Seleccione un item de la orden para ajustar la cantidad.");
        }
    }

    @FXML
    private void removeOrderItem() {
        OrderItem selectedItem = orderTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            currentOrder.remove(selectedItem);
            updateTotal();
        } else {
            showAlert("Advertencia", "Seleccione un item de la orden para eliminar.");
        }
    }

    @FXML
    private void confirmOrder() {
        if (currentOrder.isEmpty()) {
            showAlert("Advertencia", "La orden está vacía.");
            return;
        }
        // In a real app, this would save the order to the database.
        showAlert("Éxito", "Orden confirmada exitosamente.");
        currentOrder.clear();
        updateTotal();
    }

    @FXML
    private void cancelOrder() {
        if (!currentOrder.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cancelar Orden");
            alert.setHeaderText("¿Está seguro que desea cancelar la orden actual?");
            alert.setContentText("Todos los items serán eliminados de la orden.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                currentOrder.clear();
                updateTotal();
            }
        }
    }

    private void updateTotal() {
        double total = currentOrder.stream().mapToDouble(OrderItem::getTotalPrice).sum();
        totalLabel.setText(String.format("$%.2f", total));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
