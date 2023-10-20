-- Migration: Create customers table
-- Description: Customer master records with Mexican naming conventions and behavioral analytics

-- Enable accent-insensitive search functionality
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Create immutable wrapper for unaccent (required for generated columns)
CREATE OR REPLACE FUNCTION immutable_unaccent(text)
RETURNS text AS $$
    SELECT unaccent($1);
$$ LANGUAGE sql IMMUTABLE;

CREATE TABLE customers (
    id SERIAL PRIMARY KEY,

    -- Mexican naming convention
    first_name TEXT NOT NULL,              -- "Juan" or "Juan Carlos"
    paternal_last_name TEXT,               -- "García" (apellido paterno)
    maternal_last_name TEXT,               -- "López" (apellido materno)

    -- Generated search field (uppercase, no accents, all name components)
    full_name_search TEXT GENERATED ALWAYS AS (
        immutable_unaccent(upper(
            first_name || ' ' ||
            COALESCE(paternal_last_name, '') || ' ' ||
            COALESCE(maternal_last_name, '')
        ))
    ) STORED,

    -- Staff intelligence
    recognition_notes TEXT,                -- "Corner table family, good tippers"

    -- Behavioral profile (aggregated from orders)
    typical_party_size INTEGER,
    typical_age_distribution JSONB,        -- ['40s','40s','10-15','10-15']
    favorite_items JSONB,                  -- ['carne asada', 'fish tacos']
    typical_visit_days JSONB,              -- ['Thursday','Sunday']
    typical_visit_times JSONB,             -- ['19:00-20:00']
    avg_ticket_size REAL,
    total_lifetime_value REAL,
    visit_frequency_days REAL,             -- Average days between visits

    -- Standard fields
    notes TEXT,                            -- General customer notes
    registration_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    master_id INTEGER,                     -- For duplicate merging
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'merged')),

    -- Sync fields (for future cloud integration)
    sync_version INTEGER DEFAULT 1,
    last_synced_at TIMESTAMPTZ,
    synced_to_cloud BOOLEAN NOT NULL DEFAULT FALSE,
    cloud_id TEXT,

    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (master_id) REFERENCES customers(id) ON DELETE SET NULL
);

-- Fast search on any name component
CREATE INDEX idx_customer_name_search ON customers(full_name_search);
CREATE INDEX idx_customer_first_name ON customers(LOWER(first_name));
CREATE INDEX idx_customer_paternal ON customers(LOWER(paternal_last_name));
CREATE INDEX idx_customer_maternal ON customers(LOWER(maternal_last_name));

-- Status and duplicate management
CREATE INDEX idx_customers_status ON customers(status);
CREATE INDEX idx_customers_master_id ON customers(master_id);

-- Registration date
CREATE INDEX idx_customers_registration_date ON customers(registration_date);

-- Sync status (for future cloud integration)
CREATE INDEX idx_customers_sync_pending ON customers(synced_to_cloud) WHERE synced_to_cloud = false;
