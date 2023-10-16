package com.bara.app.service;

import com.bara.app.db.jooq.tables.pojos.Customers;
import com.bara.app.db.jooq.tables.records.CustomersRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static com.bara.app.db.jooq.Tables.CUSTOMERS;

/**
 * Service for searching customers with fuzzy matching and multi-component name search.
 * Supports Mexican naming conventions: first_name, paternal_last_name, maternal_last_name.
 */
public class CustomerSearchService {

    /**
     * Search customers by any part of their name (first, paternal, maternal).
     * Uses the generated full_name_search column for accent-insensitive matching.
     *
     * @param conn Database connection
     * @param searchTerm Search query (can be partial name)
     * @return List of matching customers
     */
    public List<Customers> searchByName(Connection conn, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String normalizedSearch = normalizeForSearch(searchTerm);
        DSLContext dsl = DSL.using(conn);

        return dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.FULL_NAME_SEARCH.like("%" + normalizedSearch + "%"))
                .and(CUSTOMERS.STATUS.eq("active"))
                .orderBy(CUSTOMERS.FIRST_NAME.asc())
                .limit(50)
                .fetchInto(Customers.class);
    }

    /**
     * Search customers by phone number.
     * Searches across all phone records (primary and secondary).
     *
     * @param conn Database connection
     * @param phoneNumber Phone number to search
     * @return List of matching customers
     */
    public List<Customers> searchByPhone(Connection conn, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String cleanedPhone = phoneNumber.replaceAll("[^0-9]", "");
        DSLContext dsl = DSL.using(conn);

        return dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.ID.in(
                        dsl.select(com.bara.app.db.jooq.Tables.CUSTOMER_PHONES.CUSTOMER_ID)
                                .from(com.bara.app.db.jooq.Tables.CUSTOMER_PHONES)
                                .where(com.bara.app.db.jooq.Tables.CUSTOMER_PHONES.PHONE_NUMBER.like("%" + cleanedPhone + "%"))
                ))
                .and(CUSTOMERS.STATUS.eq("active"))
                .orderBy(CUSTOMERS.FIRST_NAME.asc())
                .limit(50)
                .fetchInto(Customers.class);
    }

    /**
     * Find exact customer by ID.
     *
     * @param conn Database connection
     * @param customerId Customer ID
     * @return Customer or null if not found
     */
    public Customers findById(Connection conn, int customerId) {
        DSLContext dsl = DSL.using(conn);
        CustomersRecord record = dsl.selectFrom(CUSTOMERS)
                .where(CUSTOMERS.ID.eq(customerId))
                .fetchOne();
        return record != null ? record.into(Customers.class) : null;
    }

    /**
     * Normalize search term: uppercase, remove accents.
     * Matches the logic in the generated full_name_search column.
     *
     * @param text Text to normalize
     * @return Normalized text
     */
    private String normalizeForSearch(String text) {
        return text.toUpperCase()
                .replace('Á', 'A')
                .replace('É', 'E')
                .replace('Í', 'I')
                .replace('Ó', 'O')
                .replace('Ú', 'U')
                .replace('Ñ', 'N');
    }
}
