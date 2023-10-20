package com.bara.app.service;

import com.bara.app.database.DatabaseManager;
import com.bara.app.db.jooq.tables.pojos.MenuItems;
import com.bara.app.model.MenuItem;
import com.bara.app.repository.MenuItemRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer that bridges old MenuItem model with jOOQ repository.
 */
public class MenuItemService {

    private final MenuItemRepository repository = new MenuItemRepository();

    public List<MenuItem> findAll() {
        try (Connection conn = DatabaseManager.getConnection()) {
            List<MenuItems> jooqItems = repository.findAll(conn);
            return convertToModels(jooqItems);
        } catch (SQLException e) {
            System.err.println("Error finding menu items: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public MenuItem findById(int id) {
        try (Connection conn = DatabaseManager.getConnection()) {
            MenuItems jooqItem = repository.findById(conn, id);
            return jooqItem != null ? convertToModel(jooqItem) : null;
        } catch (SQLException e) {
            System.err.println("Error finding menu item: " + e.getMessage());
            return null;
        }
    }

    public MenuItem save(MenuItem item) {
        try (Connection conn = DatabaseManager.getConnection()) {
            MenuItems jooqItem = convertToPojo(item);
            MenuItems created = repository.create(conn, jooqItem);
            return convertToModel(created);
        } catch (SQLException e) {
            System.err.println("Error saving menu item: " + e.getMessage());
            return null;
        }
    }

    public MenuItem update(MenuItem item) {
        try (Connection conn = DatabaseManager.getConnection()) {
            MenuItems jooqItem = convertToPojo(item);
            MenuItems updated = repository.update(conn, jooqItem);
            return convertToModel(updated);
        } catch (SQLException e) {
            System.err.println("Error updating menu item: " + e.getMessage());
            return null;
        }
    }

    public void delete(int id) {
        try (Connection conn = DatabaseManager.getConnection()) {
            repository.delete(conn, id);
        } catch (SQLException e) {
            System.err.println("Error deleting menu item: " + e.getMessage());
        }
    }

    private MenuItem convertToModel(MenuItems jooqItem) {
        return new MenuItem(
                jooqItem.getId(),
                jooqItem.getName(),
                jooqItem.getPrice().doubleValue(),
                jooqItem.getDescription()
        );
    }

    private List<MenuItem> convertToModels(List<MenuItems> jooqItems) {
        List<MenuItem> models = new ArrayList<>();
        for (MenuItems jooqItem : jooqItems) {
            models.add(convertToModel(jooqItem));
        }
        return models;
    }

    private MenuItems convertToPojo(MenuItem model) {
        MenuItems pojo = new MenuItems();
        if (model.getId() > 0) {
            pojo.setId(model.getId());
        }
        pojo.setName(model.getName());
        pojo.setPrice((float) model.getPrice());
        pojo.setDescription(model.getDescription());
        pojo.setIsAvailable(true);
        return pojo;
    }
}
