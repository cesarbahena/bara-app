-- Migration: Create customer_addresses table
-- Description: Advanced address management with validation, geocoding, and duplicate detection

CREATE TABLE customer_addresses (
    id SERIAL PRIMARY KEY,
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
    formatted_provider_data JSONB, -- Full provider response
    latitude REAL,
    longitude REAL,

    -- Duplicate detection
    normalized_key TEXT, -- Standardized address string for matching

    -- Validation workflow
    validation_status TEXT NOT NULL DEFAULT 'pending'
        CHECK (validation_status IN ('pending', 'validated', 'failed', 'partial', 'manual')),
    validation_metadata JSONB, -- Validation service metadata
    validation_attempted_at TIMESTAMPTZ,
    validation_completed_at TIMESTAMPTZ,

    -- Address preferences
    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    -- Delivery notes
    delivery_instructions TEXT,

    -- Timestamps
    added_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

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
