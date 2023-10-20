package com.bara.app.repository;

import com.bara.app.db.jooq.tables.pojos.CustomerPhones;
import com.bara.app.db.jooq.tables.records.CustomerPhonesRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.List;

import static com.bara.app.db.jooq.Tables.CUSTOMER_PHONES;

public class CustomerPhoneRepository {

    public CustomerPhones create(Connection conn, CustomerPhones phone) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        CustomerPhonesRecord record = dsl.newRecord(CUSTOMER_PHONES);
        record.setCustomerId(phone.getCustomerId());
        record.setPhoneNumber(phone.getPhoneNumber());
        record.setIsPrimary(phone.getIsPrimary() != null ? phone.getIsPrimary() : false);
        record.setIsDefaultSearch(phone.getIsDefaultSearch() != null ? phone.getIsDefaultSearch() : true);
        record.setAddedDate(now);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        record.store();
        return record.into(CustomerPhones.class);
    }

    public List<CustomerPhones> findByCustomerId(Connection conn, int customerId) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(CUSTOMER_PHONES)
                .where(CUSTOMER_PHONES.CUSTOMER_ID.eq(customerId))
                .orderBy(CUSTOMER_PHONES.IS_PRIMARY.desc())
                .fetchInto(CustomerPhones.class);
    }

    public CustomerPhones findPrimaryPhone(Connection conn, int customerId) {
        DSLContext dsl = DSL.using(conn);
        CustomerPhonesRecord record = dsl.selectFrom(CUSTOMER_PHONES)
                .where(CUSTOMER_PHONES.CUSTOMER_ID.eq(customerId))
                .and(CUSTOMER_PHONES.IS_PRIMARY.eq(true))
                .fetchOne();
        return record != null ? record.into(CustomerPhones.class) : null;
    }

    public void setPrimary(Connection conn, int phoneId) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        CustomerPhonesRecord phone = dsl.selectFrom(CUSTOMER_PHONES)
                .where(CUSTOMER_PHONES.ID.eq(phoneId))
                .fetchOne();

        if (phone != null) {
            dsl.update(CUSTOMER_PHONES)
                    .set(CUSTOMER_PHONES.IS_PRIMARY, false)
                    .where(CUSTOMER_PHONES.CUSTOMER_ID.eq(phone.getCustomerId()))
                    .execute();

            dsl.update(CUSTOMER_PHONES)
                    .set(CUSTOMER_PHONES.IS_PRIMARY, true)
                    .set(CUSTOMER_PHONES.UPDATED_AT, now)
                    .where(CUSTOMER_PHONES.ID.eq(phoneId))
                    .execute();
        }
    }
}
