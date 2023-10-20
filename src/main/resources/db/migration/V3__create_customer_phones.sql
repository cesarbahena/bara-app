-- Migration: Create customer_phones table
-- Description: Phone numbers associated with customers

CREATE TABLE customer_phones (
    id SERIAL PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    phone_number TEXT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE, -- true=primary, false=secondary
    is_default_search BOOLEAN NOT NULL DEFAULT FALSE, -- true=use for search, false=don't
    added_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key to customers
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,

    -- Ensure unique phone per customer
    UNIQUE (customer_id, phone_number)
);

-- Index for customer lookup (most common query)
CREATE INDEX idx_customer_phones_customer_id ON customer_phones(customer_id);

-- Index for phone number search (reverse lookup)
CREATE INDEX idx_customer_phones_number ON customer_phones(phone_number);

-- Index for finding primary phones
CREATE INDEX idx_customer_phones_primary ON customer_phones(customer_id, is_primary);

-- Index for search-enabled phones
CREATE INDEX idx_customer_phones_searchable ON customer_phones(is_default_search, phone_number);
