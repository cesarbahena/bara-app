-- Migration: Create customers table
-- Description: Customer master records with Mexican naming conventions and behavioral analytics

CREATE TABLE customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- Mexican naming convention
    first_name TEXT NOT NULL,              -- "Juan" or "Juan Carlos"
    paternal_last_name TEXT,               -- "García" (apellido paterno)
    maternal_last_name TEXT,               -- "López" (apellido materno)

    -- Generated search field (uppercase, no accents, all name components)
    full_name_search TEXT GENERATED ALWAYS AS (
        upper(
            REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                first_name || ' ' ||
                COALESCE(paternal_last_name, '') || ' ' ||
                COALESCE(maternal_last_name, ''),
            'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U'),'Ñ','N')
        )
    ) STORED,

    -- Staff intelligence
    recognition_notes TEXT,                -- "Corner table family, good tippers"

    -- Behavioral profile (aggregated from orders)
    typical_party_size INTEGER,
    typical_age_distribution TEXT,         -- JSON: ['40s','40s','10-15','10-15']
    favorite_items TEXT,                   -- JSON: ['carne asada', 'fish tacos']
    typical_visit_days TEXT,               -- JSON: ['Thursday','Sunday']
    typical_visit_times TEXT,              -- JSON: ['19:00-20:00']
    avg_ticket_size REAL,
    total_lifetime_value REAL,
    visit_frequency_days REAL,             -- Average days between visits

    -- Standard fields
    notes TEXT,                            -- General customer notes
    registration_date TEXT NOT NULL DEFAULT (datetime('now')),
    master_id INTEGER,                     -- For duplicate merging
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'merged')),

    -- Sync fields (for future cloud integration)
    sync_version INTEGER DEFAULT 1,
    last_synced_at TEXT,
    synced_to_cloud INTEGER DEFAULT 0 CHECK (synced_to_cloud IN (0, 1)),
    cloud_id TEXT,

    -- Timestamps
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),

    FOREIGN KEY (master_id) REFERENCES customers(id) ON DELETE SET NULL
);

-- Fast search on any name component
CREATE INDEX idx_customer_name_search ON customers(full_name_search);
CREATE INDEX idx_customer_first_name ON customers(first_name COLLATE NOCASE);
CREATE INDEX idx_customer_paternal ON customers(paternal_last_name COLLATE NOCASE);
CREATE INDEX idx_customer_maternal ON customers(maternal_last_name COLLATE NOCASE);

-- Status and duplicate management
CREATE INDEX idx_customers_status ON customers(status);
CREATE INDEX idx_customers_master_id ON customers(master_id);

-- Registration date
CREATE INDEX idx_customers_registration_date ON customers(registration_date);

-- Sync status (for future cloud integration)
CREATE INDEX idx_customers_sync_pending ON customers(synced_to_cloud) WHERE synced_to_cloud = 0;
