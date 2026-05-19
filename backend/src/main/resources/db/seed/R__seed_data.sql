-- Interest tags for recommendations and catalog
INSERT INTO tags (slug, label) VALUES
    ('outdoor', 'Outdoor'),
    ('family', 'Family'),
    ('networking', 'Networking'),
    ('educational', 'Educational');

-- Checkout flow sample data (event id 1 is referenced by the frontend checkout demo)
INSERT INTO users (email, password_hash, full_name, phone, is_guest, created_at, updated_at)
VALUES ('organizer@test.com', 'hash', 'John Organizer', '+37061234567', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('sarah@example.com', 'hash', 'Sarah Vilnius', '+37060000001', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('john@example.com', 'hash', 'John Dubai', '+37060000002', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('milda@example.com', 'hash', 'Milda Petrauskaite', '+37060000003', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('tomas@example.com', 'hash', 'Tomas Kazlauskas', '+37060000004', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('eva@example.com', 'hash', 'Eva Jensen', '+37060000005', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO organizers (user_id, business_name, description, verified, created_at, updated_at)
VALUES (1, 'Paradise Events', 'Top event organizer in Vilnius', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO categories (name, slug, created_at, updated_at)
VALUES ('Music', 'music', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('Sports', 'sports', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('Arts', 'arts', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('Technology', 'technology', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('Food', 'food', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('Family', 'family', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO venues (organizer_id, name, address_line1, city, country, rating, created_at, updated_at)
VALUES (1, 'Grand Hall', 'Gedimino pr. 1', 'Vilnius', 'LT', 4.50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Paradise Hall', 'Konstitucijos pr. 12', 'Vilnius', 'LT', 4.70, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'North Pier Studio', 'Upes g. 9', 'Vilnius', 'LT', 4.40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Central Park Arena', 'Parko g. 15', 'Vilnius', 'LT', 4.30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Riverfront Lawn', 'Zveju g. 4', 'Vilnius', 'LT', 4.60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Creative Campus', 'Svitrigailos g. 29', 'Vilnius', 'LT', 4.80, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'City Arena', 'Ozo g. 18', 'Vilnius', 'LT', 4.55, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Old Town Stage', 'Vokieciu g. 6', 'Vilnius', 'LT', 4.65, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Gallery District', 'Literatu g. 5', 'Vilnius', 'LT', 4.35, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'South Field', 'Stadiono g. 2', 'Vilnius', 'LT', 4.20, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Tech Hub', 'Lvivo g. 21', 'Vilnius', 'LT', 4.75, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Market Square', 'Hales g. 1', 'Vilnius', 'LT', 4.25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Botanical Garden Pavilion', 'Kairenu g. 43', 'Vilnius', 'LT', 4.85, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Cinema Terrace', 'A. Gostauto g. 11', 'Vilnius', 'LT', 4.30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Lakeside Amphitheater', 'Traku g. 30', 'Trakai', 'LT', 4.90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Night Market Yard', 'Pylimo g. 58', 'Vilnius', 'LT', 4.45, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO events (organizer_id, venue_id, category_id, title, slug, description, status, start_datetime, end_datetime, timezone, min_age, created_at, updated_at)
VALUES (1, 1, 1, 'Summer Fest', 'summer-fest', 'The biggest summer festival in Vilnius with live bands, food stalls, and late evening performances.', 'ACTIVE', '2026-07-01 18:00:00', '2026-07-01 23:00:00', 'Europe/Vilnius', 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 2, 1, 'Riverside Jazz Night', 'riverside-jazz-night', 'A warm evening of modern jazz, classic standards, and riverside cocktails.', 'ACTIVE', '2026-06-12 19:00:00', '2026-06-12 22:30:00', 'Europe/Vilnius', 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 3, 4, 'Startup Founders Mixer', 'startup-founders-mixer', 'A focused networking night for founders, operators, investors, and builders.', 'ACTIVE', '2026-06-13 18:30:00', '2026-06-13 21:30:00', 'Europe/Vilnius', 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 4, 5, 'Family Food Festival', 'family-food-festival', 'Street food, local producers, cooking demos, and activities for families.', 'ACTIVE', '2026-06-14 11:00:00', '2026-06-14 18:00:00', 'Europe/Vilnius', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 5, 3, 'Open Air Cinema', 'open-air-cinema', 'An outdoor film night with reserved lawn seating and local snacks.', 'ACTIVE', '2026-06-18 20:30:00', '2026-06-18 23:30:00', 'Europe/Vilnius', 12, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 6, 4, 'Design Systems Workshop', 'design-systems-workshop', 'A practical workshop on tokens, components, governance, and product design operations.', 'ACTIVE', '2026-06-19 10:00:00', '2026-06-19 16:00:00', 'Europe/Vilnius', 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 7, 2, 'City Arena Finals', 'city-arena-finals', 'The season finals with courtside seats, fan zones, and half-time entertainment.', 'ACTIVE', '2026-06-20 17:00:00', '2026-06-20 21:00:00', 'Europe/Vilnius', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 8, 1, 'Acoustic Sessions', 'acoustic-sessions', 'An intimate lineup of singer-songwriters performing stripped-back sets in the old town.', 'ACTIVE', '2026-06-21 19:30:00', '2026-06-21 22:00:00', 'Europe/Vilnius', 14, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 9, 3, 'Modern Art Walk', 'modern-art-walk', 'A guided evening through installations, galleries, artist talks, and pop-up exhibitions.', 'ACTIVE', '2026-06-22 18:00:00', '2026-06-22 21:00:00', 'Europe/Vilnius', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 10, 2, 'Junior Football Camp', 'junior-football-camp', 'A weekend skills camp for young players with coaches, drills, and friendly matches.', 'ACTIVE', '2026-06-27 09:00:00', '2026-06-27 15:00:00', 'Europe/Vilnius', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 11, 4, 'Cloud Engineering Forum', 'cloud-engineering-forum', 'Talks and panels covering platform engineering, observability, security, and cloud cost control.', 'ACTIVE', '2026-06-30 09:30:00', '2026-06-30 17:30:00', 'Europe/Vilnius', 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 12, 5, 'Street Food Showcase', 'street-food-showcase', 'A tasting event featuring food trucks, chefs, local drinks, and market specials.', 'ACTIVE', '2026-07-03 12:00:00', '2026-07-03 20:00:00', 'Europe/Vilnius', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 13, 6, 'Garden Family Picnic', 'garden-family-picnic', 'A relaxed family day with music, garden games, workshops, and picnic baskets.', 'ACTIVE', '2026-07-05 10:00:00', '2026-07-05 16:00:00', 'Europe/Vilnius', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 14, 3, 'Indie Film Premiere', 'indie-film-premiere', 'A local film premiere with director Q&A, terrace seating, and after-screening discussion.', 'ACTIVE', '2026-07-10 19:00:00', '2026-07-10 22:30:00', 'Europe/Vilnius', 16, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 15, 1, 'Lakeside Electronic Live', 'lakeside-electronic-live', 'A lakeside electronic music show with live synths, visuals, and sunset sets.', 'ACTIVE', '2026-07-18 18:00:00', '2026-07-18 23:45:00', 'Europe/Vilnius', 18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 16, 5, 'Night Market Live', 'night-market-live', 'An evening market with live DJs, small plates, dessert stands, and late-night shopping.', 'ACTIVE', '2026-07-24 18:00:00', '2026-07-24 23:00:00', 'Europe/Vilnius', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO ticket_types (event_id, name, description, price, currency, quantity_total, quantity_sold, sale_start, sale_end, max_per_order, is_active, created_at, updated_at)
VALUES (1, 'General Admission', 'Access to the main festival area.', 39.00, 'USD', 250, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'VIP Ticket', 'Priority entry and VIP viewing area.', 150.00, 'USD', 50, 0, NULL, NULL, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (1, 'Student Ticket', 'Discounted admission with valid student ID.', 30.00, 'USD', 100, 0, NULL, NULL, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'Standard Seat', 'Reserved standard seating.', 42.00, 'USD', 160, 0, NULL, NULL, 8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'Front Row', 'Front row reserved seating.', 85.00, 'USD', 24, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 'Balcony Seat', 'Balcony seating with a full stage view.', 55.00, 'USD', 48, 0, NULL, NULL, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 'Mixer Pass', 'Entry to talks and networking.', 25.00, 'USD', 180, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 'Founder Pass', 'Entry plus founder roundtable access.', 60.00, 'USD', 60, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 'Investor Pass', 'Entry plus investor lounge access.', 95.00, 'USD', 30, 0, NULL, NULL, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 'Adult Entry', 'Festival entry for one adult.', 18.00, 'USD', 300, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 'Child Entry', 'Festival entry for one child.', 8.00, 'USD', 250, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 'Family Pack', 'Entry for two adults and two children.', 45.00, 'USD', 120, 0, NULL, NULL, 5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 'Lawn Seat', 'Bring a blanket and enjoy the film.', 16.00, 'USD', 220, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 'Deck Chair', 'Reserved deck chair seating.', 28.00, 'USD', 80, 0, NULL, NULL, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 'Couples Set', 'Two deck chairs and snack vouchers.', 50.00, 'USD', 50, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (6, 'Workshop Seat', 'Full-day workshop admission.', 120.00, 'USD', 90, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (6, 'Team Seat', 'Discounted seat for team bookings.', 95.00, 'USD', 60, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (6, 'Student Seat', 'Student workshop admission.', 55.00, 'USD', 30, 0, NULL, NULL, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (7, 'Arena Seat', 'Standard arena seating.', 35.00, 'USD', 500, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (7, 'Courtside', 'Premium courtside seating.', 140.00, 'USD', 36, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (7, 'Fan Zone', 'Standing fan zone access.', 22.00, 'USD', 300, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (8, 'General Seat', 'Unreserved seating.', 24.00, 'USD', 130, 0, NULL, NULL, 8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (8, 'Table Seat', 'Shared table seating near the stage.', 38.00, 'USD', 40, 0, NULL, NULL, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (8, 'Artist Supporter', 'Admission plus artist support contribution.', 55.00, 'USD', 25, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (9, 'Walk Pass', 'Guided gallery walk access.', 20.00, 'USD', 160, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (9, 'Collector Pass', 'Walk pass plus collector reception.', 65.00, 'USD', 35, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (9, 'Student Pass', 'Discounted guided walk access.', 12.00, 'USD', 80, 0, NULL, NULL, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (10, 'Player Pass', 'Camp participation for one junior player.', 32.00, 'USD', 140, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (10, 'Sibling Pass', 'Discounted additional player pass.', 24.00, 'USD', 80, 0, NULL, NULL, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (10, 'Spectator Pass', 'Entry for a parent or spectator.', 6.00, 'USD', 200, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (11, 'Forum Pass', 'Full forum access.', 75.00, 'USD', 260, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (11, 'Workshop Add-on', 'Forum access plus hands-on lab.', 145.00, 'USD', 80, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (11, 'Startup Pass', 'Discounted access for startup teams.', 45.00, 'USD', 120, 0, NULL, NULL, 8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (12, 'Tasting Pass', 'Entry with five tasting tokens.', 19.00, 'USD', 320, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (12, 'Chef Table', 'Reserved chef demo seating and tastings.', 70.00, 'USD', 42, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (12, 'Market Entry', 'Entry without tasting tokens.', 8.00, 'USD', 400, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (13, 'Picnic Entry', 'General garden entry.', 14.00, 'USD', 280, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (13, 'Family Basket', 'Entry plus picnic basket for four.', 58.00, 'USD', 70, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (13, 'Workshop Add-on', 'Craft workshop add-on.', 18.00, 'USD', 100, 0, NULL, NULL, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (14, 'Screening Seat', 'Reserved screening seat.', 22.00, 'USD', 180, 0, NULL, NULL, 8, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (14, 'Premiere Pass', 'Screening plus Q&A priority seating.', 48.00, 'USD', 70, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (14, 'Terrace Pair', 'Two seats with terrace snack vouchers.', 76.00, 'USD', 40, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (15, 'Lakeside Entry', 'General access to the lakeside show.', 44.00, 'USD', 420, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (15, 'Stage Front', 'Stage-front standing area.', 90.00, 'USD', 120, 0, NULL, NULL, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (15, 'Sunset Lounge', 'Lounge access with dedicated bar.', 135.00, 'USD', 60, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (16, 'Market Entry', 'Entry to the night market.', 12.00, 'USD', 500, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (16, 'Tasting Pass', 'Entry plus six tasting tokens.', 32.00, 'USD', 260, 0, NULL, NULL, 10, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (16, 'Late Table', 'Reserved shared table with tasting tokens.', 68.00, 'USD', 55, 0, NULL, NULL, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO discount_code (event_id, code, type, discount_value, active, max_redemptions, used_count, expires_at)
VALUES (1, 'SAVE10', 'PERCENT', 10.00, TRUE, 100, 0, NULL),
       (2, 'JAZZ10', 'PERCENT', 10.00, TRUE, 80, 0, NULL),
       (3, 'BUILD15', 'PERCENT', 15.00, TRUE, 60, 0, NULL),
       (4, 'FAMILY5', 'FIXED', 5.00, TRUE, 150, 0, NULL),
       (5, 'CINEMA10', 'PERCENT', 10.00, TRUE, 100, 0, NULL),
       (6, 'DESIGN20', 'PERCENT', 20.00, TRUE, 50, 0, NULL),
       (7, 'FINALS10', 'PERCENT', 10.00, TRUE, 100, 0, NULL),
       (8, 'ACOUSTIC5', 'FIXED', 5.00, TRUE, 80, 0, NULL),
       (9, 'ART10', 'PERCENT', 10.00, TRUE, 80, 0, NULL),
       (10, 'CAMP5', 'FIXED', 5.00, TRUE, 120, 0, NULL),
       (11, 'CLOUD15', 'PERCENT', 15.00, TRUE, 90, 0, NULL),
       (12, 'TASTE10', 'PERCENT', 10.00, TRUE, 120, 0, NULL),
       (13, 'PICNIC5', 'FIXED', 5.00, TRUE, 100, 0, NULL),
       (14, 'FILM10', 'PERCENT', 10.00, TRUE, 80, 0, NULL),
       (15, 'LAKE15', 'PERCENT', 15.00, TRUE, 100, 0, NULL),
       (16, 'MARKET10', 'PERCENT', 10.00, TRUE, 140, 0, NULL);

INSERT INTO reviews (user_id, event_id, venue_id, rating, comment, created_at, updated_at)
VALUES (2, 1, 1, 4.50, 'Amazing event!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 1, 1, 5.00, 'Best night of my life!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 2, 2, 4.80, 'Beautiful venue and excellent sound.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 2, 2, 4.60, 'Really smooth evening with great performers.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (6, 3, 3, 4.70, 'Useful conversations and a strong founder crowd.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 4, 4, 4.40, 'Plenty of food choices and easy with kids.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 5, 5, 4.30, 'Relaxed setup and comfortable outdoor screening.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 6, 6, 4.90, 'Practical workshop with clear examples.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 7, 7, 4.60, 'Great atmosphere from start to finish.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (6, 8, 8, 4.50, 'Intimate and very well curated.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 9, 9, 4.20, 'Nice route and friendly gallery hosts.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 10, 10, 4.30, 'The coaches kept the kids engaged all day.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 11, 11, 4.80, 'Strong talks and useful technical detail.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 12, 12, 4.40, 'Good variety and fast entry.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (6, 13, 13, 4.70, 'Perfect location for a family day.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (2, 14, 14, 4.50, 'Great Q&A after the screening.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, 15, 15, 4.90, 'The lakeside setting made the show memorable.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, 15, 15, 4.80, 'Visuals and sound were excellent.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, 16, 16, 4.50, 'Fun food selection and good late-night energy.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (6, 16, 16, 4.40, 'Easygoing crowd and quick service at the stands.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Recommendations demo user, venues, published events, and preferences
INSERT INTO users (email, password_hash, full_name, phone, is_guest, created_at, updated_at)
VALUES ('alex@demo.local', 'hash', 'Alex Demo', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO organizers (user_id, business_name, description, verified, created_at, updated_at)
VALUES (7, 'Paradise Events', 'Demo organizer for recommendations', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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
    (2, 17, (SELECT id FROM categories WHERE slug = 'music'), 'Jazz Night at Loftas', 'jazz-night-at-loftas', 'An intimate evening with local and touring jazz quartets.', 'PUBLISHED', DATEADD('DAY', 3, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 3, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 18, (SELECT id FROM categories WHERE slug = 'music'), 'Open-Air Rock Festival', 'open-air-rock-festival', 'Three stages, a dozen bands, one unforgettable night under the stars.', 'PUBLISHED', DATEADD('DAY', 8, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 8, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 19, (SELECT id FROM categories WHERE slug = 'music'), 'Chamber Music Sunday', 'chamber-music-sunday', 'A cozy afternoon of Bach, Mozart and Ravel performed by the city quartet.', 'PUBLISHED', DATEADD('DAY', 21, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 21, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 20, (SELECT id FROM categories WHERE slug = 'sports'), 'City Marathon 2026', 'city-marathon-2026', 'Join 8,000 runners across the city''s most scenic route.', 'PUBLISHED', DATEADD('DAY', 10, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 10, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 21, (SELECT id FROM categories WHERE slug = 'sports'), 'Basketball Derby Night', 'basketball-derby-night', 'The rivalry continues at full capacity.', 'PUBLISHED', DATEADD('DAY', 5, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 5, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 22, (SELECT id FROM categories WHERE slug = 'sports'), 'Sunrise Trail Run', 'sunrise-trail-run', 'A guided 10k trail run through the forest at dawn.', 'PUBLISHED', DATEADD('DAY', 14, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 14, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 23, (SELECT id FROM categories WHERE slug = 'arts'), 'Contemporary Art Opening', 'contemporary-art-opening', 'Opening night of the new season''s contemporary art exhibition.', 'PUBLISHED', DATEADD('DAY', 2, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 2, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 24, (SELECT id FROM categories WHERE slug = 'arts'), 'Street Art Walking Tour', 'street-art-walking-tour', 'A guided stroll through the city''s most striking murals and installations.', 'PUBLISHED', DATEADD('DAY', 12, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 12, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 25, (SELECT id FROM categories WHERE slug = 'arts'), 'Pottery Workshop for Beginners', 'pottery-workshop-for-beginners', 'Hands-on introduction to the wheel.', 'PUBLISHED', DATEADD('DAY', 18, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 18, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 26, (SELECT id FROM categories WHERE slug = 'technology'), 'AI Builders Meetup', 'ai-builders-meetup', 'Lightning talks and demos from local AI engineers.', 'PUBLISHED', DATEADD('DAY', 4, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 4, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 27, (SELECT id FROM categories WHERE slug = 'technology'), 'DevOps Summit 2026', 'devops-summit-2026', 'A full day of talks on platform engineering, SRE and observability.', 'PUBLISHED', DATEADD('DAY', 25, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 25, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 28, (SELECT id FROM categories WHERE slug = 'technology'), 'Startup Pitch Night', 'startup-pitch-night', 'Ten early-stage startups pitch to a panel of local investors.', 'PUBLISHED', DATEADD('DAY', 9, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 9, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 29, (SELECT id FROM categories WHERE slug = 'food'), 'Street Food Festival', 'street-food-festival', 'Food trucks, craft beer and live music in the old town square.', 'PUBLISHED', DATEADD('DAY', 6, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 6, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 30, (SELECT id FROM categories WHERE slug = 'food'), 'Pasta Masterclass', 'pasta-masterclass', 'Learn to make three classic handmade pastas from scratch.', 'PUBLISHED', DATEADD('DAY', 15, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 15, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 31, (SELECT id FROM categories WHERE slug = 'food'), 'Wine Tasting Evening', 'wine-tasting-evening', 'A sommelier-led tasting across six regions of Italy.', 'PUBLISHED', DATEADD('DAY', 20, CURRENT_TIMESTAMP), DATEADD('HOUR', 3, DATEADD('DAY', 20, CURRENT_TIMESTAMP)), 'Europe/Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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
VALUES (7, 'Vilnius', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_preferred_categories (preferences_id, category_id)
SELECT p.id, c.id
FROM user_preferences p
JOIN categories c ON c.slug IN ('music', 'technology')
WHERE p.user_id = 7;

INSERT INTO user_preferred_tags (preferences_id, tag_id)
SELECT p.id, t.id
FROM user_preferences p
JOIN tags t ON t.slug IN ('outdoor', 'networking')
WHERE p.user_id = 7;
