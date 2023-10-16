package com.bara.app.repository;

import com.bara.app.db.jooq.tables.pojos.Customers;
import com.bara.app.db.jooq.tables.records.CustomersRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.bara.app.db.jooq.Tables.CUSTOMERS;

/**
 * Repository for customer data access using jOOQ.
 */
public class CustomerRepository {

    private static final DateTimeFormatter SQLITE_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Create a new customer.
     *
     * @param conn Database connection
     * @param customer Customer data
     * @return Created customer with generated ID
     */
    public Customers create(Connection conn, Customers customer) {
        DSLContext dsl = DSL.using(conn);

        String now = LocalDateTime.now().format(SQLITE_DATETIME_FORMAT);

        CustomersRecord record = dsl.newRecord(CUSTOMERS);
        record.setFirstName(customer.getFirstName());
        record.setPaternalLastName(customer.getPaternalLastName());
        record.setMaternalLastName(customer.getMaternalLastName());
        record.setRecognitionNotes(customer.getRecognitionNotes());
        record.setNotes(customer.getNotes());
        record.setStatus(customer.getStatus() != null ? customer.getStatus() : "active");
        record.setRegistrationDate(now);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setSyncVersion(1);
        record.setSyncedToCloud(0);

        record.store();
        return record.into(Customers.class);
    }

    /**
     * Update an existing customer.
     *
     * @param conn Database connection
     * @param customer Customer data with ID
     * @return Updated customer
     */
    public Customers update(Connection conn, Customers customer) {
        DSLContext dsl = DSL.using(conn);

        String now = LocalDateTime.now().format(SQLITE_DATETIME_FORMAT);

        int updated = dsl.update(CUSTOMERS)
                .set(CUSTOMERS.FIRST_NAME, customer.getFirstName())
                .set(CUSTOMERS.PATERNAL_LAST_NAME, customer.getPaternalLastName())
                .set(CUSTOMERS.MATERNAL_LAST_NAME, customer.getMaternalLastName())
                .set(CUSTOMERS.RECOGNITION_NOTES, customer.getRecognitionNotes())
                .set(CUSTOMERS.NOTES, customer.getNotes())
                .set(CUSTOMERS.STATUS, customer.getStatus())
                .set(CUSTOMERS.UPDATED_AT, now)
                .set(CUSTOMERS.SYNC_VERSION, CUSTOMERS.SYNC_VERSION.plus(1))
                .where(CUSTOMERS.ID.eq(customer.getId()))
                .execute();

        return updated > 0 ? findById(conn, customer.getId()) : null;
    }

    /**
     * Find customer by ID.
     *
     * @param conn Database connection
     * @param id Customer ID
     * @return Customer or null if not found
     */
    public Customers findById(Connection conn, int id) {
        DSLContext dsl = DSL.using(conn);
        CustomersRecord record = dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.ID.eq(id))
                .fetchOne();
        return record != null ? record.into(Customers.class) : null;
    }

    /**
     * Find all active customers.
     *
     * @param conn Database connection
     * @return List of active customers
     */
    public List<Customers> findAllActive(Connection conn) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.STATUS.eq("active"))
                .orderBy(CUSTOMERS.FIRST_NAME.asc())
                .fetchInto(Customers.class);
    }

    /**
     * Mark customer as inactive (soft delete).
     *
     * @param conn Database connection
     * @param customerId Customer ID
     */
    public void markInactive(Connection conn, int customerId) {
        DSLContext dsl = DSL.using(conn);
        String now = LocalDateTime.now().format(SQLITE_DATETIME_FORMAT);

        dsl.update(CUSTOMERS)
                .set(CUSTOMERS.STATUS, "inactive")
                .set(CUSTOMERS.UPDATED_AT, now)
                .where(CUSTOMERS.ID.eq(customerId))
                .execute();
    }
}
