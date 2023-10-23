package com.bara.app.repository;

import com.bara.app.db.jooq.tables.pojos.UnidentifiedCustomerClusters;
import com.bara.app.db.jooq.tables.records.UnidentifiedCustomerClustersRecord;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.List;

import static com.bara.app.db.jooq.Tables.UNIDENTIFIED_CUSTOMER_CLUSTERS;

/**
 * Repository for unidentified customer clusters (pattern recognition).
 * Supports retroactive customer matching when phone numbers are provided.
 */
public class UnidentifiedClusterRepository {

    /**
     * Create a new cluster.
     */
    public UnidentifiedCustomerClusters create(Connection conn, UnidentifiedCustomerClusters cluster) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        UnidentifiedCustomerClustersRecord record = dsl.newRecord(UNIDENTIFIED_CUSTOMER_CLUSTERS);
        record.setNamePattern(cluster.getNamePattern());
        record.setTypicalPartySize(cluster.getTypicalPartySize());
        record.setTypicalPartyComposition(cluster.getTypicalPartyComposition());
        record.setFrequentItems(cluster.getFrequentItems());
        record.setTypicalDays(cluster.getTypicalDays());
        record.setTypicalTimes(cluster.getTypicalTimes());
        record.setOrderCount(cluster.getOrderCount() != null ? cluster.getOrderCount() : 0);
        record.setTotalSpent(cluster.getTotalSpent() != null ? cluster.getTotalSpent() : 0.0f);
        record.setAvgTicketSize(cluster.getAvgTicketSize());
        record.setFirstSeen(cluster.getFirstSeen() != null ? cluster.getFirstSeen() : now);
        record.setLastSeen(cluster.getLastSeen() != null ? cluster.getLastSeen() : now);
        record.setPatternConfidence(cluster.getPatternConfidence() != null ? cluster.getPatternConfidence() : 0.5f);
        record.setHasPartyPattern(cluster.getHasPartyPattern() != null ? cluster.getHasPartyPattern() : false);
        record.setHasTemporalPattern(cluster.getHasTemporalPattern() != null ? cluster.getHasTemporalPattern() : false);
        record.setHasItemPreferences(cluster.getHasItemPreferences() != null ? cluster.getHasItemPreferences() : false);
        record.setStaffNotes(cluster.getStaffNotes());
        record.setRecognitionHints(cluster.getRecognitionHints());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);

        record.store();
        return record.into(UnidentifiedCustomerClusters.class);
    }

    /**
     * Find cluster by ID.
     */
    public UnidentifiedCustomerClusters findById(Connection conn, int id) {
        DSLContext dsl = DSL.using(conn);
        UnidentifiedCustomerClustersRecord record = dsl.selectFrom(UNIDENTIFIED_CUSTOMER_CLUSTERS)
                .where(UNIDENTIFIED_CUSTOMER_CLUSTERS.ID.eq(id))
                .fetchOne();
        return record != null ? record.into(UnidentifiedCustomerClusters.class) : null;
    }

    /**
     * Find unmatched clusters sorted by info quality (best candidates for matching).
     */
    public List<UnidentifiedCustomerClusters> findUnmatched(Connection conn, int limit) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(UNIDENTIFIED_CUSTOMER_CLUSTERS)
                .where(UNIDENTIFIED_CUSTOMER_CLUSTERS.MATCHED_CUSTOMER_ID.isNull())
                .orderBy(UNIDENTIFIED_CUSTOMER_CLUSTERS.INFO_QUALITY_SCORE.desc(),
                        UNIDENTIFIED_CUSTOMER_CLUSTERS.ORDER_COUNT.desc())
                .limit(limit)
                .fetchInto(UnidentifiedCustomerClusters.class);
    }

    /**
     * Find high-confidence clusters (likely same customer).
     */
    public List<UnidentifiedCustomerClusters> findHighConfidence(Connection conn, float minConfidence) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(UNIDENTIFIED_CUSTOMER_CLUSTERS)
                .where(UNIDENTIFIED_CUSTOMER_CLUSTERS.MATCHED_CUSTOMER_ID.isNull())
                .and(UNIDENTIFIED_CUSTOMER_CLUSTERS.PATTERN_CONFIDENCE.ge(minConfidence))
                .orderBy(UNIDENTIFIED_CUSTOMER_CLUSTERS.PATTERN_CONFIDENCE.desc())
                .fetchInto(UnidentifiedCustomerClusters.class);
    }

    /**
     * Find clusters by name pattern (for matching suggestions).
     */
    public List<UnidentifiedCustomerClusters> findByNamePattern(Connection conn, String namePattern) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(UNIDENTIFIED_CUSTOMER_CLUSTERS)
                .where(UNIDENTIFIED_CUSTOMER_CLUSTERS.NAME_PATTERN.likeIgnoreCase("%" + namePattern + "%"))
                .and(UNIDENTIFIED_CUSTOMER_CLUSTERS.MATCHED_CUSTOMER_ID.isNull())
                .orderBy(UNIDENTIFIED_CUSTOMER_CLUSTERS.ORDER_COUNT.desc())
                .fetchInto(UnidentifiedCustomerClusters.class);
    }

    /**
     * Find clusters by party size (helps narrow down matches).
     */
    public List<UnidentifiedCustomerClusters> findByPartySize(Connection conn, int partySize) {
        DSLContext dsl = DSL.using(conn);
        return dsl.selectFrom(UNIDENTIFIED_CUSTOMER_CLUSTERS)
                .where(UNIDENTIFIED_CUSTOMER_CLUSTERS.TYPICAL_PARTY_SIZE.eq(partySize))
                .and(UNIDENTIFIED_CUSTOMER_CLUSTERS.MATCHED_CUSTOMER_ID.isNull())
                .orderBy(UNIDENTIFIED_CUSTOMER_CLUSTERS.PATTERN_CONFIDENCE.desc())
                .fetchInto(UnidentifiedCustomerClusters.class);
    }

    /**
     * Find recently seen unmatched clusters (active customers).
     */
    public List<UnidentifiedCustomerClusters> findRecentlyActive(Connection conn, int daysBack, int limit) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(daysBack);

        return dsl.selectFrom(UNIDENTIFIED_CUSTOMER_CLUSTERS)
                .where(UNIDENTIFIED_CUSTOMER_CLUSTERS.MATCHED_CUSTOMER_ID.isNull())
                .and(UNIDENTIFIED_CUSTOMER_CLUSTERS.LAST_SEEN.ge(cutoff))
                .orderBy(UNIDENTIFIED_CUSTOMER_CLUSTERS.LAST_SEEN.desc())
                .limit(limit)
                .fetchInto(UnidentifiedCustomerClusters.class);
    }

    /**
     * Link cluster to customer (retroactive matching).
     */
    public void linkToCustomer(Connection conn, int clusterId, int customerId, String matchMethod, float confidence) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        dsl.update(UNIDENTIFIED_CUSTOMER_CLUSTERS)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.MATCHED_CUSTOMER_ID, customerId)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.MATCHED_AT, now)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.MATCHED_BY, matchMethod)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.MATCH_CONFIDENCE, confidence)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.UPDATED_AT, now)
                .where(UNIDENTIFIED_CUSTOMER_CLUSTERS.ID.eq(clusterId))
                .execute();
    }

    /**
     * Update cluster statistics (called by background job).
     */
    public void updateStatistics(Connection conn, int clusterId, int orderCount, float totalSpent,
                                  float avgTicketSize, OffsetDateTime lastSeen) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        dsl.update(UNIDENTIFIED_CUSTOMER_CLUSTERS)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.ORDER_COUNT, orderCount)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.TOTAL_SPENT, totalSpent)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.AVG_TICKET_SIZE, avgTicketSize)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.LAST_SEEN, lastSeen)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.UPDATED_AT, now)
                .where(UNIDENTIFIED_CUSTOMER_CLUSTERS.ID.eq(clusterId))
                .execute();
    }

    /**
     * Update pattern confidence score (recalculated by background job).
     */
    public void updatePatternConfidence(Connection conn, int clusterId, float confidence,
                                        boolean hasPartyPattern, boolean hasTemporalPattern, boolean hasItemPreferences) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        dsl.update(UNIDENTIFIED_CUSTOMER_CLUSTERS)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.PATTERN_CONFIDENCE, confidence)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.HAS_PARTY_PATTERN, hasPartyPattern)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.HAS_TEMPORAL_PATTERN, hasTemporalPattern)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.HAS_ITEM_PREFERENCES, hasItemPreferences)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.UPDATED_AT, now)
                .where(UNIDENTIFIED_CUSTOMER_CLUSTERS.ID.eq(clusterId))
                .execute();
    }

    /**
     * Add staff notes to cluster (helps with manual matching).
     */
    public void updateStaffNotes(Connection conn, int clusterId, String notes) {
        DSLContext dsl = DSL.using(conn);
        OffsetDateTime now = OffsetDateTime.now();

        dsl.update(UNIDENTIFIED_CUSTOMER_CLUSTERS)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.STAFF_NOTES, notes)
                .set(UNIDENTIFIED_CUSTOMER_CLUSTERS.UPDATED_AT, now)
                .where(UNIDENTIFIED_CUSTOMER_CLUSTERS.ID.eq(clusterId))
                .execute();
    }

    /**
     * Delete cluster (used when merging or cleaning up low-quality clusters).
     */
    public void delete(Connection conn, int clusterId) {
        DSLContext dsl = DSL.using(conn);
        dsl.deleteFrom(UNIDENTIFIED_CUSTOMER_CLUSTERS)
                .where(UNIDENTIFIED_CUSTOMER_CLUSTERS.ID.eq(clusterId))
                .execute();
    }
}
