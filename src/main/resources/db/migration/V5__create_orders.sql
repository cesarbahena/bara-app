-- Migration: Create orders and order_items tables
-- Description: Order management with customer tracking, party composition, and pattern recognition

CREATE TABLE orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER,                   -- NULLABLE! (anonymous orders supported)
    cluster_id INTEGER,                    -- Link to unidentified_customer_clusters
    customer_address_id INTEGER,

    -- Order classification
    order_type TEXT NOT NULL DEFAULT 'delivery' CHECK (order_type IN ('delivery', 'pickup', 'dine_in')),

    -- Anonymous order fallback
    order_name TEXT,                       -- "Juan" if customer_id IS NULL (for kitchen display)

    -- Party composition (for analytics)
    party_size INTEGER,
    party_composition TEXT,                -- JSON: [{'age':'40s'},{'age':'40s'},{'age':'10-15'},{'age':'10-15'}]

    -- Context capture (for pattern recognition)
    table_number TEXT,                     -- "14" or "Mesa 14"
    day_of_week TEXT,                      -- Auto-filled: 'Monday', 'Tuesday', etc.
    time_of_day TEXT,                      -- Auto-filled: '19:30'

    -- Address snapshot (preserve delivery address at order time)
    raw_address_snapshot TEXT,
    formatted_address_snapshot TEXT,

    -- Order status
    status TEXT NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'confirmed', 'preparing', 'ready', 'out_for_delivery', 'delivered', 'cancelled')),

    -- Financial
    subtotal REAL NOT NULL CHECK (subtotal >= 0),
    tax REAL NOT NULL DEFAULT 0 CHECK (tax >= 0),
    delivery_fee REAL NOT NULL DEFAULT 0 CHECK (delivery_fee >= 0),
    total REAL NOT NULL CHECK (total >= 0),

    -- Payment
    payment_status TEXT NOT NULL DEFAULT 'unpaid'
        CHECK (payment_status IN ('unpaid', 'paid', 'refunded', 'partial')),
    payment_method TEXT CHECK (payment_method IN ('cash', 'card', 'transfer', 'other')),
    paid_at TEXT,

    -- Notes
    customer_notes TEXT,
    internal_notes TEXT,
    staff_observations TEXT,               -- "Regular corner table, asked for extra salsa"

    -- Sync fields (for future cloud integration)
    sync_version INTEGER DEFAULT 1,
    last_synced_at TEXT,
    synced_to_cloud INTEGER DEFAULT 0 CHECK (synced_to_cloud IN (0, 1)),
    cloud_id TEXT,

    -- Timestamps
    ordered_at TEXT NOT NULL DEFAULT (datetime('now')),
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),

    -- Foreign keys
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    FOREIGN KEY (customer_address_id) REFERENCES customer_addresses(id) ON DELETE SET NULL
);

CREATE TABLE order_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id INTEGER NOT NULL,
    menu_item_id INTEGER NOT NULL,

    -- Item details (snapshot at order time)
    item_name TEXT NOT NULL,               -- Preserve name even if menu item changes
    item_price REAL NOT NULL CHECK (item_price >= 0), -- Preserve price at order time

    -- Quantity and customization
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    notes TEXT,                            -- Special requests (e.g., "no onions")

    -- Line item total
    line_total REAL NOT NULL CHECK (line_total >= 0),

    -- Timestamps
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at TEXT NOT NULL DEFAULT (datetime('now')),

    -- Foreign keys
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE RESTRICT
);

-- Indexes for orders table
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_cluster_id ON orders(cluster_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_ordered_at ON orders(ordered_at);
CREATE INDEX idx_orders_customer_ordered_at ON orders(customer_id, ordered_at DESC);
CREATE INDEX idx_orders_type ON orders(order_type);

-- Pattern matching indexes
CREATE INDEX idx_orders_party_pattern ON orders(party_size, day_of_week, time_of_day);
CREATE INDEX idx_orders_anonymous ON orders(order_name, party_size) WHERE customer_id IS NULL;

-- Sync status
CREATE INDEX idx_orders_sync_pending ON orders(synced_to_cloud) WHERE synced_to_cloud = 0;

-- Indexes for order_items table
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items(menu_item_id);
