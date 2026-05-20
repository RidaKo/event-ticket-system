ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER';
UPDATE users SET role = 'GUEST' WHERE is_guest = TRUE;

ALTER TABLE purchase_orders ADD COLUMN user_id INT;
ALTER TABLE purchase_orders
    ADD CONSTRAINT fk_purchase_orders_user
    FOREIGN KEY (user_id) REFERENCES users(id);
