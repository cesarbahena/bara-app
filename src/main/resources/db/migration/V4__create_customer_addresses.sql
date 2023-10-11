-- Migration: Create customer_addresses table
-- Description: Advanced address management with validation, geocoding, and duplicate detection

CREATE TABLE customer_addresses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,

    -- Raw user input
    raw_input TEXT NOT NULL,

    -- Structured address components (parsed from raw input)
    street TEXT,
    city TEXT,
    state TEXT,
    postal_code TEXT,
    country TEXT DEFAULT 'MX',

    -- Provider validation data (from geocoding/validation service)
    formatted_provider_data TEXT, -- JSON with full provider response
    latitude REAL,
    longitude REAL,

    -- Duplicate detection
    normalized_key TEXT, -- Standardized address string for matching

    -- Validation workflow
    validation_status TEXT NOT NULL DEFAULT 'pending'
        CHECK (validation_status IN ('pending', 'validated', 'failed', 'partial', 'manual')),
    validation_metadata TEXT, -- JSON with validation service metadata
    validation_attempted_at TEXT,
    validation_completed_at TEXT,

    -- Address preferences
    is_default INTEGER NOT NULL DEFAULT 0 CHECK (is_default IN (0, 1)),

    -- Delivery notes
    delivery_instructions TEXT,

    -- Timestamps
    added_date TEXT NOT NULL DEFAULT (datetime('now')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),

    -- Foreign key to customers
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Index for customer lookup (most common query)
CREATE INDEX idx_customer_addresses_customer_id ON customer_addresses(customer_id);

-- Index for default address lookup
CREATE INDEX idx_customer_addresses_default ON customer_addresses(customer_id, is_default);

-- Index for validation status (for background processing)
CREATE INDEX idx_customer_addresses_validation_status ON customer_addresses(validation_status);

-- Index for duplicate detection using normalized key
CREATE INDEX idx_customer_addresses_normalized_key ON customer_addresses(normalized_key);

-- Index for geographic searches (if needed later)
CREATE INDEX idx_customer_addresses_location ON customer_addresses(latitude, longitude);

-- Index for city-based queries
CREATE INDEX idx_customer_addresses_city ON customer_addresses(city);
