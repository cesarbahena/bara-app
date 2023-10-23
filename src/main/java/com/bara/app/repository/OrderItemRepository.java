package com.bara.app.repository;

import com.bara.app.db.jooq.tables.pojos.OrderItems;
import com.bara.app.db.jooq.tables.records.OrderItemsRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.List;

import static com.bara.app.db.jooq.Tables.ORDER_ITEMS;

/**
 * Repository for order items data access using jOOQ.
 */
public class OrderItemRepository {

    /**
     * Create a new order item.
     */
    public OrderItems create(Connection conn, OrderItems item) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        OrderItemsRecord record = dsl.newRecord(ORDER_ITEMS);
        record.setOrderId(item.getOrderId());
        record.setMenuItemId(item.getMenuItemId());
        record.setItemName(item.getItemName());
        record.setItemPrice(item.getItemPrice());
        record.setQuantity(item.getQuantity());
        record.setNotes(item.getNotes());
        record.setLineTotal(item.getLineTotal());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        record.store();
        return record.into(OrderItems.class);
    }

    /**
     * Find order item by ID.
     */
    public OrderItems findById(Connection conn, int id) {
        DSLContext dsl = DSL.using(conn);
        OrderItemsRecord record = dsl.selectFrom(ORDER_ITEMS)
                .where(ORDER_ITEMS.ID.eq(id))
                .fetchOne();
        return record != null ? record.into(OrderItems.class) : null;
    }

    /**
     * Find all items for a specific order.
     */
    public List<OrderItems> findByOrderId(Connection conn, int orderId) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(ORDER_ITEMS)
                .where(ORDER_ITEMS.ORDER_ID.eq(orderId))
                .orderBy(ORDER_ITEMS.ID.asc())
                .fetchInto(OrderItems.class);
    }

    /**
     * Update order item quantity and recalculate line total.
     */
    public OrderItems updateQuantity(Connection conn, int itemId, int newQuantity) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        // Fetch current item to get price
        OrderItemsRecord item = dsl.selectFrom(ORDER_ITEMS)
                .where(ORDER_ITEMS.ID.eq(itemId))
                .fetchOne();

        if (item != null) {
            float newLineTotal = item.getItemPrice() * newQuantity;

            dsl.update(ORDER_ITEMS)
                    .set(ORDER_ITEMS.QUANTITY, newQuantity)
                    .set(ORDER_ITEMS.LINE_TOTAL, newLineTotal)
                    .set(ORDER_ITEMS.UPDATED_AT, now)
                    .where(ORDER_ITEMS.ID.eq(itemId))
                    .execute();

            return findById(conn, itemId);
        }

        return null;
    }

    /**
     * Update order item notes.
     */
    public void updateNotes(Connection conn, int itemId, String notes) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        dsl.update(ORDER_ITEMS)
                .set(ORDER_ITEMS.NOTES, notes)
                .set(ORDER_ITEMS.UPDATED_AT, now)
                .where(ORDER_ITEMS.ID.eq(itemId))
                .execute();
    }

    /**
     * Delete order item.
     */
    public void delete(Connection conn, int itemId) {
        DSLContext dsl = DSL.using(conn);
        dsl.deleteFrom(ORDER_ITEMS)
                .where(ORDER_ITEMS.ID.eq(itemId))
                .execute();
    }

    /**
     * Delete all items for an order (used when order is cancelled).
     */
    public void deleteByOrderId(Connection conn, int orderId) {
        DSLContext dsl = DSL.using(conn);
        dsl.deleteFrom(ORDER_ITEMS)
                .where(ORDER_ITEMS.ORDER_ID.eq(orderId))
                .execute();
    }

    /**
     * Calculate total for all items in an order.
     */
    public float calculateOrderTotal(Connection conn, int orderId) {
        DSLContext dsl = DSL.using(conn);
        Float total = dsl.select(DSL.sum(ORDER_ITEMS.LINE_TOTAL))
                .from(ORDER_ITEMS)
                .where(ORDER_ITEMS.ORDER_ID.eq(orderId))
                .fetchOne(0, Float.class);
        return total != null ? total : 0.0f;
    }
}
