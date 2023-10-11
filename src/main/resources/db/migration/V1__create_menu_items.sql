-- Migration: Create menu_items table
-- Description: Core menu items for the POS system

CREATE TABLE menu_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    price REAL NOT NULL CHECK (price >= 0),
    description TEXT,
    is_available INTEGER NOT NULL DEFAULT 1 CHECK (is_available IN (0, 1)), -- 1=available, 0=86'd
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Index for quick name lookups
CREATE INDEX idx_menu_items_name ON menu_items(name);

-- Index for availability filtering
CREATE INDEX idx_menu_items_available ON menu_items(is_available);
