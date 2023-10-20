-- Migration: Create unidentified_customer_clusters table
-- Description: Pattern-based clustering for anonymous orders to enable retroactive customer matching

CREATE TABLE unidentified_customer_clusters (
    id SERIAL PRIMARY KEY,

    -- Name pattern (from anonymous orders)
    name_pattern TEXT,                     -- "Juan" (most common name in this cluster)

    -- Behavioral fingerprint
    typical_party_size INTEGER,
    typical_party_composition JSONB,       -- Aggregated age distribution from party_composition
    frequent_items JSONB,                  -- ['carne asada', 'fish tacos'] (top 5-10 items)

    -- Temporal patterns
    typical_days JSONB,                    -- ['Thursday', 'Sunday']
    typical_times JSONB,                   -- ['19:00-20:00', '13:00-14:00']

    -- Aggregated statistics
    order_count INTEGER NOT NULL DEFAULT 0,
    total_spent REAL NOT NULL DEFAULT 0 CHECK (total_spent >= 0),
    avg_ticket_size REAL,
    first_seen TIMESTAMPTZ NOT NULL,       -- First order timestamp
    last_seen TIMESTAMPTZ NOT NULL,        -- Most recent order timestamp

    -- Confidence scoring (0.0 to 1.0)
    pattern_confidence REAL DEFAULT 0.5 CHECK (pattern_confidence >= 0 AND pattern_confidence <= 1),

    -- Info quality metrics (for UI sorting: "rich clusters" vs "sparse clusters")
    has_party_pattern BOOLEAN NOT NULL DEFAULT FALSE,
    has_temporal_pattern BOOLEAN NOT NULL DEFAULT FALSE,
    has_item_preferences BOOLEAN NOT NULL DEFAULT FALSE,
    info_quality_score REAL GENERATED ALWAYS AS (
        (has_party_pattern::int +
         has_temporal_pattern::int +
         has_item_preferences::int) / 3.0
    ) STORED,

    -- Matching status
    matched_customer_id INTEGER,           -- Set when cluster is linked to a customer
    matched_at TIMESTAMPTZ,                -- When the match was made
    matched_by TEXT,                       -- 'staff' or 'phone' or 'auto'
    match_confidence REAL CHECK (match_confidence >= 0 AND match_confidence <= 1),

    -- Staff intelligence
    staff_notes TEXT,                      -- "Thinks this is the corner table family"
    recognition_hints TEXT,                -- "Always asks for extra salsa, dad wears baseball cap"

    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign keys
    FOREIGN KEY (matched_customer_id) REFERENCES customers(id) ON DELETE SET NULL
);

-- Pattern search indexes
CREATE INDEX idx_clusters_name_pattern ON unidentified_customer_clusters(LOWER(name_pattern));
CREATE INDEX idx_clusters_party_size ON unidentified_customer_clusters(typical_party_size);

-- Quality and matching indexes
CREATE INDEX idx_clusters_info_quality ON unidentified_customer_clusters(info_quality_score DESC);
CREATE INDEX idx_clusters_confidence ON unidentified_customer_clusters(pattern_confidence DESC);
CREATE INDEX idx_clusters_unmatched ON unidentified_customer_clusters(matched_customer_id) WHERE matched_customer_id IS NULL;

-- Recent activity (for "seen recently" sorting)
CREATE INDEX idx_clusters_last_seen ON unidentified_customer_clusters(last_seen DESC);
CREATE INDEX idx_clusters_order_count ON unidentified_customer_clusters(order_count DESC);

-- Matched cluster tracking
CREATE INDEX idx_clusters_matched_customer ON unidentified_customer_clusters(matched_customer_id);
