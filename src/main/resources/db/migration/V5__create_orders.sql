-- Migration: Create orders and order_items tables
-- Description: Order management with customer tracking and delivery details

CREATE TABLE orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    customer_address_id INTEGER, -- May be NULL for in-store pickup orders

    -- Order type
    order_type TEXT NOT NULL DEFAULT 'delivery' CHECK (order_type IN ('delivery', 'pickup', 'dine_in')),

    -- Address snapshot (preserve delivery address at order time)
    raw_address_snapshot TEXT, -- Raw address as it was when order was placed
    formatted_address_snapshot TEXT, -- Formatted address snapshot

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
    item_name TEXT NOT NULL, -- Preserve name even if menu item changes
    item_price REAL NOT NULL CHECK (item_price >= 0), -- Preserve price at order time

    -- Quantity and customization
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    notes TEXT, -- Special requests (e.g., "no onions")

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
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_ordered_at ON orders(ordered_at);
CREATE INDEX idx_orders_customer_ordered_at ON orders(customer_id, ordered_at DESC);
CREATE INDEX idx_orders_type ON orders(order_type);

-- Indexes for order_items table
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items(menu_item_id);
