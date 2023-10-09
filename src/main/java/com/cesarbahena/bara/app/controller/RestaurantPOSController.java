package com.cesarbahena.bara.app.controller;

import com.cesarbahena.bara.app.BaraAppFX;
import com.cesarbahena.bara.app.model.MenuItem;
import com.cesarbahena.bara.app.model.OrderItem;
import com.cesarbahena.bara.app.repository.MenuItemRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.util.Optional;

public class RestaurantPOSController {

    // FXML fields from the SHELL (RestaurantPOSView.fxml)
    @FXML
    private StackPane mainContentArea;

    // FXML fields from the dynamically loaded views (TakeOrderView.fxml, etc.)
    // These are public so FXMLLoader can access them when the controller is set manually.
    @FXML
    public FlowPane menuFlowPane;
    @FXML
    public TableView<OrderItem> orderTable;
    @FXML
    public TableColumn<OrderItem, String> orderItemNameColumn;
    @FXML
    public TableColumn<OrderItem, Integer> orderItemQuantityColumn;
    @FXML
    public TableColumn<OrderItem, Double> orderItemPriceColumn;
    @FXML
    public TableColumn<OrderItem, Double> orderItemTotalPriceColumn;
    @FXML
    public TextArea itemNotesArea;
    @FXML
    public Label totalLabel;

    private MenuItemRepository menuItemRepository;
    private ObservableList<OrderItem> currentOrder;
    private boolean shellInitialized = false;

    @FXML
    public void initialize() {
        // This guard is crucial to prevent the recursive loop.
        if (shellInitialized) {
            return;
        }
        shellInitialized = true;

        menuItemRepository = new MenuItemRepository();
        currentOrder = FXCollections.observableArrayList();

        // Load the default view
        showTakeOrderView();
    }

    // --- View Navigation Methods ---

    @FXML
    public void showTakeOrderView() {
        try {
            FXMLLoader loader = new FXMLLoader(BaraAppFX.class.getResource("TakeOrderView.fxml"));
            // Set this instance as the controller. This is crucial.
            loader.setController(this);
            Node takeOrderView = loader.load();

            // The FXML is loaded, now we can use the injected fields from it.
            // This setup logic is specific to the TakeOrderView.
            setupTable();
            orderTable.setItems(currentOrder);
            orderTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> showItemNotes(newSelection));
            loadMenuItems();
            updateTotal(); // Ensure total is updated on first load

            mainContentArea.getChildren().setAll(takeOrderView);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error Crítico", "No se pudo cargar la vista de Punto de Venta.");
        }
    }

    @FXML
    public void showMenuManagementView() {
        loadView("MenuManagementView.fxml");
    }

    private void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(BaraAppFX.class.getResource(fxmlFileName));
            Node view = loader.load();
            mainContentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Error loading view: " + fxmlFileName + " - " + e.getMessage());
            e.printStackTrace();
        }
    }


    // --- Logic from the original POS controller ---

    private void setupTable() {
        orderItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("menuItemName"));
        orderItemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        orderItemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("menuItemPrice"));
        orderItemTotalPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadMenuItems() {
        // Ensure menuFlowPane is not null, which can happen if the FXML isn't loaded yet.
        if (menuFlowPane == null) return;
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
