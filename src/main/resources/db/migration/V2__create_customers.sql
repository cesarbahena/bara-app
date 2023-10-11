-- Migration: Create customers table
-- Description: Customer master records with duplicate merging support

CREATE TABLE customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    notes TEXT,
    registration_date TEXT NOT NULL DEFAULT (datetime('now')),
    master_id INTEGER, -- References another customer if this is a duplicate
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'merged')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),

    -- Foreign key for master_id (self-referencing)
    FOREIGN KEY (master_id) REFERENCES customers(id) ON DELETE SET NULL
);

-- Index for name searching (case-insensitive)
CREATE INDEX idx_customers_name ON customers(name COLLATE NOCASE);

-- Index for status filtering
CREATE INDEX idx_customers_status ON customers(status);

-- Index for finding duplicates merged to a master
CREATE INDEX idx_customers_master_id ON customers(master_id);

-- Index for registration date sorting
CREATE INDEX idx_customers_registration_date ON customers(registration_date);
