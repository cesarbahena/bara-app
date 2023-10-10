package com.bara.app.repository;

import com.bara.app.database.DatabaseManager;
import com.bara.app.model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MenuItemRepository {

    public MenuItemRepository() {
        DatabaseManager.initializeDatabase();
    }

    public void save(MenuItem menuItem) {
        String sql = "INSERT INTO menu_items(name, price, description) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, menuItem.getName());
            pstmt.setDouble(2, menuItem.getPrice());
            pstmt.setString(3, menuItem.getDescription());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    menuItem.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving menu item: " + e.getMessage());
        }
    }

    public void update(MenuItem menuItem) {
        String sql = "UPDATE menu_items SET name = ?, price = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, menuItem.getName());
            pstmt.setDouble(2, menuItem.getPrice());
            pstmt.setString(3, menuItem.getDescription());
            pstmt.setInt(4, menuItem.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM menu_items WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
        }
    }

    public Optional<MenuItem> findById(int id) {
        String sql = "SELECT id, name, price, description FROM menu_items WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new MenuItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding menu item by id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<MenuItem> findAll() {
        List<MenuItem> menuItems = new ArrayList<>();
        String sql = "SELECT id, name, price, description FROM menu_items";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                menuItems.add(new MenuItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all menu items: " + e.getMessage());
        }
        return menuItems;
    }
}
