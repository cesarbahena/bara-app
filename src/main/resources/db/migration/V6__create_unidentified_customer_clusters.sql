-- Migration: Create unidentified_customer_clusters table
-- Description: Pattern-based clustering for anonymous orders to enable retroactive customer matching

CREATE TABLE unidentified_customer_clusters (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- Name pattern (from anonymous orders)
    name_pattern TEXT,                     -- "Juan" (most common name in this cluster)

    -- Behavioral fingerprint
    typical_party_size INTEGER,
    typical_party_composition TEXT,        -- JSON: aggregated age distribution from party_composition
    frequent_items TEXT,                   -- JSON: ['carne asada', 'fish tacos'] (top 5-10 items)

    -- Temporal patterns
    typical_days TEXT,                     -- JSON: ['Thursday', 'Sunday']
    typical_times TEXT,                    -- JSON: ['19:00-20:00', '13:00-14:00']

    -- Aggregated statistics
    order_count INTEGER NOT NULL DEFAULT 0,
    total_spent REAL NOT NULL DEFAULT 0 CHECK (total_spent >= 0),
    avg_ticket_size REAL,
    first_seen TEXT NOT NULL,              -- First order timestamp
    last_seen TEXT NOT NULL,               -- Most recent order timestamp

    -- Confidence scoring (0.0 to 1.0)
    pattern_confidence REAL DEFAULT 0.5 CHECK (pattern_confidence >= 0 AND pattern_confidence <= 1),

    -- Info quality metrics (for UI sorting: "rich clusters" vs "sparse clusters")
    has_party_pattern INTEGER DEFAULT 0 CHECK (has_party_pattern IN (0, 1)),
    has_temporal_pattern INTEGER DEFAULT 0 CHECK (has_temporal_pattern IN (0, 1)),
    has_item_preferences INTEGER DEFAULT 0 CHECK (has_item_preferences IN (0, 1)),
    info_quality_score REAL GENERATED ALWAYS AS (
        (CAST(has_party_pattern AS REAL) +
         CAST(has_temporal_pattern AS REAL) +
         CAST(has_item_preferences AS REAL)) / 3.0
    ) STORED,

    -- Matching status
    matched_customer_id INTEGER,           -- Set when cluster is linked to a customer
    matched_at TEXT,                       -- When the match was made
    matched_by TEXT,                       -- 'staff' or 'phone' or 'auto'
    match_confidence REAL CHECK (match_confidence >= 0 AND match_confidence <= 1),

    -- Staff intelligence
    staff_notes TEXT,                      -- "Thinks this is the corner table family"
    recognition_hints TEXT,                -- "Always asks for extra salsa, dad wears baseball cap"

    -- Timestamps
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),

    -- Foreign keys
    FOREIGN KEY (matched_customer_id) REFERENCES customers(id) ON DELETE SET NULL
);

-- Pattern search indexes
CREATE INDEX idx_clusters_name_pattern ON unidentified_customer_clusters(name_pattern COLLATE NOCASE);
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
