package com.bara.app.repository;

import com.bara.app.db.jooq.tables.pojos.CustomerAddresses;
import com.bara.app.db.jooq.tables.records.CustomerAddressesRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.List;

import static com.bara.app.db.jooq.Tables.CUSTOMER_ADDRESSES;

/**
 * Repository for customer addresses with async validation workflow support.
 */
public class CustomerAddressRepository {

    /**
     * Create a new customer address.
     */
    public CustomerAddresses create(Connection conn, CustomerAddresses address) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        CustomerAddressesRecord record = dsl.newRecord(CUSTOMER_ADDRESSES);
        record.setCustomerId(address.getCustomerId());
        record.setRawInput(address.getRawInput());
        record.setStreet(address.getStreet());
        record.setCity(address.getCity());
        record.setState(address.getState());
        record.setPostalCode(address.getPostalCode());
        record.setCountry(address.getCountry() != null ? address.getCountry() : "MX");
        record.setFormattedProviderData(address.getFormattedProviderData());
        record.setLatitude(address.getLatitude());
        record.setLongitude(address.getLongitude());
        record.setNormalizedKey(address.getNormalizedKey());
        record.setValidationStatus(address.getValidationStatus() != null ? address.getValidationStatus() : "pending");
        record.setValidationMetadata(address.getValidationMetadata());
        record.setIsDefault(address.getIsDefault() != null ? address.getIsDefault() : false);
        record.setDeliveryInstructions(address.getDeliveryInstructions());
        record.setAddedDate(now);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        record.store();
        return record.into(CustomerAddresses.class);
    }

    /**
     * Find address by ID.
     */
    public CustomerAddresses findById(Connection conn, int id) {
        DSLContext dsl = DSL.using(conn);
        CustomerAddressesRecord record = dsl.selectFrom(CUSTOMER_ADDRESSES)
                .where(CUSTOMER_ADDRESSES.ID.eq(id))
                .fetchOne();
        return record != null ? record.into(CustomerAddresses.class) : null;
    }

    /**
     * Find all addresses for a customer.
     */
    public List<CustomerAddresses> findByCustomerId(Connection conn, int customerId) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(CUSTOMER_ADDRESSES)
                .where(CUSTOMER_ADDRESSES.CUSTOMER_ID.eq(customerId))
                .orderBy(CUSTOMER_ADDRESSES.IS_DEFAULT.desc(), CUSTOMER_ADDRESSES.ADDED_DATE.desc())
                .fetchInto(CustomerAddresses.class);
    }

    /**
     * Find customer's default address.
     */
    public CustomerAddresses findDefaultAddress(Connection conn, int customerId) {
        DSLContext dsl = DSL.using(conn);
        CustomerAddressesRecord record = dsl.selectFrom(CUSTOMER_ADDRESSES)
                .where(CUSTOMER_ADDRESSES.CUSTOMER_ID.eq(customerId))
                .and(CUSTOMER_ADDRESSES.IS_DEFAULT.eq(true))
                .fetchOne();
        return record != null ? record.into(CustomerAddresses.class) : null;
    }

    /**
     * Set address as default (unsets other defaults for same customer).
     */
    public void setAsDefault(Connection conn, int addressId) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        // Get the address to find its customer
        CustomerAddressesRecord address = dsl.selectFrom(CUSTOMER_ADDRESSES)
                .where(CUSTOMER_ADDRESSES.ID.eq(addressId))
                .fetchOne();

        if (address != null) {
            // Unset all defaults for this customer
            dsl.update(CUSTOMER_ADDRESSES)
                    .set(CUSTOMER_ADDRESSES.IS_DEFAULT, false)
                    .where(CUSTOMER_ADDRESSES.CUSTOMER_ID.eq(address.getCustomerId()))
                    .execute();

            // Set this address as default
            dsl.update(CUSTOMER_ADDRESSES)
                    .set(CUSTOMER_ADDRESSES.IS_DEFAULT, true)
                    .set(CUSTOMER_ADDRESSES.UPDATED_AT, now)
                    .where(CUSTOMER_ADDRESSES.ID.eq(addressId))
                    .execute();
        }
    }

    /**
     * Update validation status after async validation.
     */
    public void updateValidationStatus(Connection conn, int addressId, String status, org.jooq.JSONB metadata) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        dsl.update(CUSTOMER_ADDRESSES)
                .set(CUSTOMER_ADDRESSES.VALIDATION_STATUS, status)
                .set(CUSTOMER_ADDRESSES.VALIDATION_METADATA, metadata)
                .set(CUSTOMER_ADDRESSES.VALIDATION_COMPLETED_AT, now)
                .set(CUSTOMER_ADDRESSES.UPDATED_AT, now)
                .where(CUSTOMER_ADDRESSES.ID.eq(addressId))
                .execute();
    }

    /**
     * Mark validation attempt.
     */
    public void markValidationAttempted(Connection conn, int addressId) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        dsl.update(CUSTOMER_ADDRESSES)
                .set(CUSTOMER_ADDRESSES.VALIDATION_ATTEMPTED_AT, now)
                .set(CUSTOMER_ADDRESSES.UPDATED_AT, now)
                .where(CUSTOMER_ADDRESSES.ID.eq(addressId))
                .execute();
    }

    /**
     * Find addresses pending validation (for background job).
     */
    public List<CustomerAddresses> findPendingValidation(Connection conn, int limit) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(CUSTOMER_ADDRESSES)
                .where(CUSTOMER_ADDRESSES.VALIDATION_STATUS.eq("pending"))
                .and(CUSTOMER_ADDRESSES.VALIDATION_ATTEMPTED_AT.isNull()
                        .or(CUSTOMER_ADDRESSES.VALIDATION_ATTEMPTED_AT.lt(
                                DSL.field("NOW() - INTERVAL '1 hour'", OffsetDateTime.class))))
                .orderBy(CUSTOMER_ADDRESSES.ADDED_DATE.asc())
                .limit(limit)
                .fetchInto(CustomerAddresses.class);
    }

    /**
     * Find potential duplicate addresses by normalized key.
     */
    public List<CustomerAddresses> findByNormalizedKey(Connection conn, String normalizedKey) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(CUSTOMER_ADDRESSES)
                .where(CUSTOMER_ADDRESSES.NORMALIZED_KEY.eq(normalizedKey))
                .fetchInto(CustomerAddresses.class);
    }

    /**
     * Update delivery instructions.
     */
    public void updateDeliveryInstructions(Connection conn, int addressId, String instructions) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        dsl.update(CUSTOMER_ADDRESSES)
                .set(CUSTOMER_ADDRESSES.DELIVERY_INSTRUCTIONS, instructions)
                .set(CUSTOMER_ADDRESSES.UPDATED_AT, now)
                .where(CUSTOMER_ADDRESSES.ID.eq(addressId))
                .execute();
    }

    /**
     * Delete address.
     */
    public void delete(Connection conn, int addressId) {
        DSLContext dsl = DSL.using(conn);
        dsl.deleteFrom(CUSTOMER_ADDRESSES)
                .where(CUSTOMER_ADDRESSES.ID.eq(addressId))
                .execute();
    }
}
