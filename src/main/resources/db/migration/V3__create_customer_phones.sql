-- Migration: Create customer_phones table
-- Description: Phone numbers associated with customers

CREATE TABLE customer_phones (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    phone_number TEXT NOT NULL,
    is_primary INTEGER NOT NULL DEFAULT 0 CHECK (is_primary IN (0, 1)), -- 1=primary, 0=secondary
    is_default_search INTEGER NOT NULL DEFAULT 0 CHECK (is_default_search IN (0, 1)), -- 1=use for search, 0=don't
    added_date TEXT NOT NULL DEFAULT (datetime('now')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),

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
