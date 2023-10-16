package com.bara.app.repository;

import com.bara.app.db.jooq.tables.pojos.Orders;
import com.bara.app.db.jooq.tables.records.OrdersRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.bara.app.db.jooq.Tables.ORDERS;

public class OrderRepository {

    private static final DateTimeFormatter SQLITE_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Orders create(Connection conn, Orders order) {
        DSLContext dsl = DSL.using(conn);
        String now = LocalDateTime.now().format(SQLITE_DATETIME_FORMAT);

        OrdersRecord record = dsl.newRecord(ORDERS);
        record.setCustomerId(order.getCustomerId());
        record.setClusterId(order.getClusterId());
        record.setCustomerAddressId(order.getCustomerAddressId());
        record.setOrderType(order.getOrderType() != null ? order.getOrderType() : "delivery");
        record.setOrderName(order.getOrderName());
        record.setPartySize(order.getPartySize());
        record.setPartyComposition(order.getPartyComposition());
        record.setTableNumber(order.getTableNumber());
        record.setDayOfWeek(LocalDateTime.now().getDayOfWeek().toString());
        record.setTimeOfDay(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        record.setStatus(order.getStatus() != null ? order.getStatus() : "pending");
        record.setSubtotal(order.getSubtotal());
        record.setTax(order.getTax() != null ? order.getTax().floatValue() : 0.0f);
        record.setDeliveryFee(order.getDeliveryFee() != null ? order.getDeliveryFee().floatValue() : 0.0f);
        record.setTotal(order.getTotal());
        record.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus() : "unpaid");
        record.setPaymentMethod(order.getPaymentMethod());
        record.setCustomerNotes(order.getCustomerNotes());
        record.setInternalNotes(order.getInternalNotes());
        record.setStaffObservations(order.getStaffObservations());
        record.setOrderedAt(now);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setSyncVersion(1);
        record.setSyncedToCloud(0);

        record.store();
        return record.into(Orders.class);
    }

    public Orders findById(Connection conn, int orderId) {
        DSLContext dsl = DSL.using(conn);
        OrdersRecord record = dsl.selectFrom(ORDERS)
                .where(ORDERS.ID.eq(orderId))
                .fetchOne();
        return record != null ? record.into(Orders.class) : null;
    }

    public List<Orders> findByCustomerId(Connection conn, int customerId) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(ORDERS)
                .where(ORDERS.CUSTOMER_ID.eq(customerId))
                .orderBy(ORDERS.ORDERED_AT.desc())
                .fetchInto(Orders.class);
    }

    public List<Orders> findByClusterId(Connection conn, int clusterId) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(ORDERS)
                .where(ORDERS.CLUSTER_ID.eq(clusterId))
                .orderBy(ORDERS.ORDERED_AT.desc())
                .fetchInto(Orders.class);
    }

    public List<Orders> findAnonymousOrders(Connection conn) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(ORDERS)
                .where(ORDERS.CUSTOMER_ID.isNull())
                .and(ORDERS.CLUSTER_ID.isNull())
                .orderBy(ORDERS.ORDERED_AT.desc())
                .limit(100)
                .fetchInto(Orders.class);
    }

    public Orders updateStatus(Connection conn, int orderId, String status) {
        DSLContext dsl = DSL.using(conn);
        String now = LocalDateTime.now().format(SQLITE_DATETIME_FORMAT);

        dsl.update(ORDERS)
                .set(ORDERS.STATUS, status)
                .set(ORDERS.UPDATED_AT, now)
                .set(ORDERS.SYNC_VERSION, ORDERS.SYNC_VERSION.plus(1))
                .where(ORDERS.ID.eq(orderId))
                .execute();

        return findById(conn, orderId);
    }

    public void linkToCustomer(Connection conn, int orderId, int customerId) {
        DSLContext dsl = DSL.using(conn);
        String now = LocalDateTime.now().format(SQLITE_DATETIME_FORMAT);

        dsl.update(ORDERS)
                .set(ORDERS.CUSTOMER_ID, customerId)
                .set(ORDERS.CLUSTER_ID, (Integer) null)
                .set(ORDERS.UPDATED_AT, now)
                .set(ORDERS.SYNC_VERSION, ORDERS.SYNC_VERSION.plus(1))
                .where(ORDERS.ID.eq(orderId))
                .execute();
    }
}
