INSERT INTO users (email, password_hash, full_name, phone, is_guest, created_at, updated_at)
VALUES ('organizer@test.com', 'hash', 'John Organizer', '+37061234567', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO organizers (user_id, business_name, description, verified, created_at, updated_at)
VALUES (1, 'Paradise Events', 'Top event organizer in Vilnius', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO venues (organizer_id, name, address_line1, city, country, rating, created_at, updated_at)
VALUES (1, 'Grand Hall', 'Gedimino pr. 1', 'Vilnius', 'LT', 4.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO categories (name, slug, created_at, updated_at)
VALUES ('Music', 'music', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO events (organizer_id, venue_id, category_id, title, slug, description, status, start_datetime, end_datetime, timezone, min_age, created_at, updated_at)
VALUES (1, 1, 1, 'Summer Fest', 'summer-fest', 'The biggest summer festival in Vilnius', 'ACTIVE', '2026-07-01 18:00:00', '2026-07-01 23:00:00', 'Europe/Vilnius', 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ticket_types (event_id, name, description, price, currency, quantity_total, quantity_sold, sale_start, sale_end, max_per_order, is_active, created_at, updated_at)
VALUES (1, 'General Admission', NULL, 39.00, 'USD', 250, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'VIP Ticket', NULL, 150.00, 'USD', 50, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Student Ticket', NULL, 30.00, 'USD', 100, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO discount_code (event_id, code, type, discount_value, active, max_redemptions, used_count, expires_at)
VALUES (1, 'SAVE10', 'PERCENT', 10.00, TRUE, 100, 0, NULL);

INSERT INTO reviews (user_id, event_id, venue_id, rating, comment, created_at, updated_at)
VALUES (1, 1, 1, 4.50, 'Amazing event!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 1, 1, 5.00, 'Best night of my life!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
