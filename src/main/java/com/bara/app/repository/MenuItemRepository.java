package com.bara.app.repository;

import com.bara.app.db.jooq.tables.pojos.MenuItems;
import com.bara.app.db.jooq.tables.records.MenuItemsRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.bara.app.db.jooq.Tables.MENU_ITEMS;

public class MenuItemRepository {

    private static final DateTimeFormatter SQLITE_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MenuItems create(Connection conn, MenuItems item) {
        DSLContext dsl = DSL.using(conn);
        String now = LocalDateTime.now().format(SQLITE_DATETIME_FORMAT);

        MenuItemsRecord record = dsl.newRecord(MENU_ITEMS);
        record.setName(item.getName());
        record.setDescription(item.getDescription());
        record.setPrice(item.getPrice());
        record.setIsAvailable(item.getIsAvailable() != null ? item.getIsAvailable() : 1);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        record.store();
        return record.into(MenuItems.class);
    }

    public MenuItems update(Connection conn, MenuItems item) {
        DSLContext dsl = DSL.using(conn);
        String now = LocalDateTime.now().format(SQLITE_DATETIME_FORMAT);

        dsl.update(MENU_ITEMS)
                .set(MENU_ITEMS.NAME, item.getName())
                .set(MENU_ITEMS.DESCRIPTION, item.getDescription())
                .set(MENU_ITEMS.PRICE, item.getPrice())
                .set(MENU_ITEMS.IS_AVAILABLE, item.getIsAvailable())
                .set(MENU_ITEMS.UPDATED_AT, now)
                .where(MENU_ITEMS.ID.eq(item.getId()))
                .execute();

        return findById(conn, item.getId());
    }

    public MenuItems findById(Connection conn, int id) {
        DSLContext dsl = DSL.using(conn);
        MenuItemsRecord record = dsl.selectFrom(MENU_ITEMS)
                .where(MENU_ITEMS.ID.eq(id))
                .fetchOne();
        return record != null ? record.into(MenuItems.class) : null;
    }

    public List<MenuItems> findAll(Connection conn) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(MENU_ITEMS)
                .orderBy(MENU_ITEMS.NAME.asc())
                .fetchInto(MenuItems.class);
    }

    public List<MenuItems> findAvailable(Connection conn) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(MENU_ITEMS)
                .where(MENU_ITEMS.IS_AVAILABLE.eq(1))
                .orderBy(MENU_ITEMS.NAME.asc())
                .fetchInto(MenuItems.class);
    }

    public void setAvailability(Connection conn, int itemId, boolean available) {
        DSLContext dsl = DSL.using(conn);
        String now = LocalDateTime.now().format(SQLITE_DATETIME_FORMAT);

        dsl.update(MENU_ITEMS)
                .set(MENU_ITEMS.IS_AVAILABLE, available ? 1 : 0)
                .set(MENU_ITEMS.UPDATED_AT, now)
                .where(MENU_ITEMS.ID.eq(itemId))
                .execute();
    }

    public void delete(Connection conn, int itemId) {
        DSLContext dsl = DSL.using(conn);
        dsl.deleteFrom(MENU_ITEMS)
                .where(MENU_ITEMS.ID.eq(itemId))
                .execute();
    }
}
