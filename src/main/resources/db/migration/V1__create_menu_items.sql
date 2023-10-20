-- Migration: Create menu_items table
-- Description: Core menu items for the POS system

CREATE TABLE menu_items (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    price REAL NOT NULL CHECK (price >= 0),
    description TEXT,
    is_available BOOLEAN NOT NULL DEFAULT TRUE, -- TRUE=available, FALSE=86'd
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for quick name lookups
CREATE INDEX idx_menu_items_name ON menu_items(name);

-- Index for availability filtering
CREATE INDEX idx_menu_items_available ON menu_items(is_available);
