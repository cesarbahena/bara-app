package com.cesarbahena.bara.app.model;

import java.util.Objects;

public class OrderItem {
    private MenuItem menuItem;
    private int quantity;
    private String notes; // For special requests

    public OrderItem(MenuItem menuItem, int quantity) {
        this(menuItem, quantity, "");
    }

    public OrderItem(MenuItem menuItem, int quantity, String notes) {
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.notes = notes;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public double getTotalPrice() {
        return menuItem.getPrice() * quantity;
    }

    public String getMenuItemName() {
        return menuItem.getName();
    }

    public double getMenuItemPrice() {
        return menuItem.getPrice();
    }

    @Override
    public String toString() {
        return quantity + "x " + menuItem.getName() + (notes != null && !notes.isEmpty() ? " (" + notes + ")" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return quantity == orderItem.quantity && Objects.equals(menuItem, orderItem.menuItem) && Objects.equals(notes, orderItem.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuItem, quantity, notes);
    }
}
