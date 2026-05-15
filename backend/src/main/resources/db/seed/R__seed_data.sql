-- Interest tags for recommendations and catalog
INSERT INTO tags (slug, label) VALUES
    ('outdoor', 'Outdoor'),
    ('family', 'Family'),
    ('networking', 'Networking'),
    ('educational', 'Educational');

-- Checkout flow sample data (event id 1 is referenced by the frontend checkout demo)
INSERT INTO users (email, password_hash, full_name, phone, is_guest, created_at, updated_at)
VALUES ('organizer@test.com', 'hash', 'John Organizer', '+37061234567', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO organizers (user_id, business_name, description, verified, created_at, updated_at)
VALUES (1, 'Paradise Events', 'Top event organizer in Vilnius', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO venues (organizer_id, name, address_line1, city, country, rating, created_at, updated_at)
VALUES (1, 'Grand Hall', 'Gedimino pr. 1', 'Vilnius', 'LT', 4.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO events (organizer_id, venue_id, category_id, title, slug, description, status, start_datetime, end_datetime, timezone, min_age, created_at, updated_at)
VALUES (
    1,
    1,
    (SELECT id FROM categories WHERE slug = 'music'),
    'Summer Fest',
    'summer-fest',
    'The biggest summer festival in Vilnius',
    'ACTIVE',
    TIMESTAMP '2026-07-01 18:00:00',
    TIMESTAMP '2026-07-01 23:00:00',
    'Europe/Vilnius',
    18,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO ticket_types (event_id, name, description, price, currency, quantity_total, quantity_sold, sale_start, sale_end, max_per_order, is_active, created_at, updated_at)
VALUES (1, 'General Admission', NULL, 39.00, 'USD', 250, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'VIP Ticket', NULL, 150.00, 'USD', 50, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Student Ticket', NULL, 30.00, 'USD', 100, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO discount_code (event_id, code, type, discount_value, active, max_redemptions, used_count, expires_at)
VALUES (1, 'SAVE10', 'PERCENT', 10.00, TRUE, 100, 0, NULL);

INSERT INTO reviews (user_id, event_id, venue_id, rating, comment, created_at, updated_at)
VALUES (1, 1, 1, 4.50, 'Amazing event!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 1, 1, 5.00, 'Best night of my life!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Recommendations demo user, venues, published events, and preferences
INSERT INTO users (email, password_hash, full_name, phone, is_guest, created_at, updated_at)
VALUES ('alex@demo.local', 'hash', 'Alex Demo', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO organizers (user_id, business_name, description, verified, created_at, updated_at)
VALUES (2, 'Paradise Events', 'Demo organizer for recommendations', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO venues (organizer_id, name, address_line1, city, country, created_at, updated_at) VALUES
    (2, 'Loftas', 'Loftas', 'Vilnius', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Santaka Park', 'Santaka Park', 'Kaunas', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Philharmonic Hall', 'Philharmonic Hall', 'Vilnius', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'City Centre', 'City Centre', 'Vilnius', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Zalgirio Arena', 'Zalgirio Arena', 'Kaunas', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Trakai Forest', 'Trakai Forest', 'Trakai', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'MO Museum', 'MO Museum', 'Vilnius', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Uzupis District', 'Uzupis District', 'Vilnius', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Craft House', 'Craft House', 'Kaunas', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Tech Park', 'Tech Park', 'Vilnius', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Radisson Blu', 'Radisson Blu', 'Vilnius', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'KTU Startup Space', 'KTU Startup Space', 'Kaunas', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Cathedral Square', 'Cathedral Square', 'Vilnius', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Culinary Studio', 'Culinary Studio', 'Vilnius', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Old Cellar', 'Old Cellar', 'Klaipeda', 'LT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO events (organizer_id, venue_id, category_id, title, slug, description, status, start_datetime, end_datetime, timezone, created_at, updated_at) VALUES
    (2, 2, (SELECT id FROM categories WHERE slug = 'music'), 'Jazz Night at Loftas', 'jazz-night-at-loftas', 'An intimate evening with local and touring jazz quartets.', 'PUBLISHED', DATEADD('DAY', 3, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 3, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 3, (SELECT id FROM categories WHERE slug = 'music'), 'Open-Air Rock Festival', 'open-air-rock-festival', 'Three stages, a dozen bands, one unforgettable night under the stars.', 'PUBLISHED', DATEADD('DAY', 8, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 8, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 4, (SELECT id FROM categories WHERE slug = 'music'), 'Chamber Music Sunday', 'chamber-music-sunday', 'A cozy afternoon of Bach, Mozart and Ravel performed by the city quartet.', 'PUBLISHED', DATEADD('DAY', 21, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 21, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 5, (SELECT id FROM categories WHERE slug = 'sports'), 'City Marathon 2026', 'city-marathon-2026', 'Join 8,000 runners across the city''s most scenic route.', 'PUBLISHED', DATEADD('DAY', 10, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 10, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 6, (SELECT id FROM categories WHERE slug = 'sports'), 'Basketball Derby Night', 'basketball-derby-night', 'The rivalry continues at full capacity.', 'PUBLISHED', DATEADD('DAY', 5, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 5, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 7, (SELECT id FROM categories WHERE slug = 'sports'), 'Sunrise Trail Run', 'sunrise-trail-run', 'A guided 10k trail run through the forest at dawn.', 'PUBLISHED', DATEADD('DAY', 14, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 14, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 8, (SELECT id FROM categories WHERE slug = 'arts'), 'Contemporary Art Opening', 'contemporary-art-opening', 'Opening night of the new season''s contemporary art exhibition.', 'PUBLISHED', DATEADD('DAY', 2, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 2, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 9, (SELECT id FROM categories WHERE slug = 'arts'), 'Street Art Walking Tour', 'street-art-walking-tour', 'A guided stroll through the city''s most striking murals and installations.', 'PUBLISHED', DATEADD('DAY', 12, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 12, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 10, (SELECT id FROM categories WHERE slug = 'arts'), 'Pottery Workshop for Beginners', 'pottery-workshop-for-beginners', 'Hands-on introduction to the wheel.', 'PUBLISHED', DATEADD('DAY', 18, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 18, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 11, (SELECT id FROM categories WHERE slug = 'technology'), 'AI Builders Meetup', 'ai-builders-meetup', 'Lightning talks and demos from local AI engineers.', 'PUBLISHED', DATEADD('DAY', 4, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 4, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 12, (SELECT id FROM categories WHERE slug = 'technology'), 'DevOps Summit 2026', 'devops-summit-2026', 'A full day of talks on platform engineering, SRE and observability.', 'PUBLISHED', DATEADD('DAY', 25, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 25, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 13, (SELECT id FROM categories WHERE slug = 'technology'), 'Startup Pitch Night', 'startup-pitch-night', 'Ten early-stage startups pitch to a panel of local investors.', 'PUBLISHED', DATEADD('DAY', 9, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 9, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 14, (SELECT id FROM categories WHERE slug = 'food'), 'Street Food Festival', 'street-food-festival', 'Food trucks, craft beer and live music in the old town square.', 'PUBLISHED', DATEADD('DAY', 6, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 6, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 15, (SELECT id FROM categories WHERE slug = 'food'), 'Pasta Masterclass', 'pasta-masterclass', 'Learn to make three classic handmade pastas from scratch.', 'PUBLISHED', DATEADD('DAY', 15, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 15, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 16, (SELECT id FROM categories WHERE slug = 'food'), 'Wine Tasting Evening', 'wine-tasting-evening', 'A sommelier-led tasting across six regions of Italy.', 'PUBLISHED', DATEADD('DAY', 20, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 20, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug = 'networking'
WHERE e.slug = 'jazz-night-at-loftas';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug IN ('outdoor', 'family')
WHERE e.slug = 'open-air-rock-festival';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug = 'family'
WHERE e.slug = 'chamber-music-sunday';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug IN ('outdoor', 'family')
WHERE e.slug = 'city-marathon-2026';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug = 'outdoor'
WHERE e.slug = 'sunrise-trail-run';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug IN ('networking', 'educational')
WHERE e.slug = 'contemporary-art-opening';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug IN ('outdoor', 'educational')
WHERE e.slug = 'street-art-walking-tour';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug IN ('family', 'educational')
WHERE e.slug = 'pottery-workshop-for-beginners';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug IN ('networking', 'educational')
WHERE e.slug = 'ai-builders-meetup';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug IN ('networking', 'educational')
WHERE e.slug = 'devops-summit-2026';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug = 'networking'
WHERE e.slug = 'startup-pitch-night';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug IN ('outdoor', 'family')
WHERE e.slug = 'street-food-festival';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug IN ('educational', 'family')
WHERE e.slug = 'pasta-masterclass';

INSERT INTO event_tags (event_id, tag_id)
SELECT e.id, t.id
FROM events e
JOIN tags t ON t.slug = 'networking'
WHERE e.slug = 'wine-tasting-evening';

INSERT INTO user_preferences (user_id, home_city, created_at, updated_at)
VALUES (2, 'Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_preferred_categories (preferences_id, category_id)
SELECT p.id, c.id
FROM user_preferences p
JOIN categories c ON c.slug IN ('music', 'technology')
WHERE p.user_id = 2;

INSERT INTO user_preferred_tags (preferences_id, tag_id)
SELECT p.id, t.id
FROM user_preferences p
JOIN tags t ON t.slug IN ('outdoor', 'networking')
WHERE p.user_id = 2;
