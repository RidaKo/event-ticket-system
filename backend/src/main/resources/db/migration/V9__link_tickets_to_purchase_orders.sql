ALTER TABLE tickets ALTER COLUMN order_id DROP NOT NULL;

ALTER TABLE tickets ALTER COLUMN qr_code_url VARCHAR(2048);

ALTER TABLE tickets ADD COLUMN purchase_order_id BIGINT;

ALTER TABLE tickets
    ADD CONSTRAINT fk_tickets_purchase_order
        FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id);

CREATE INDEX idx_tickets_purchase_order_id ON tickets(purchase_order_id);
