-- =========================
-- ROOMS
-- =========================
INSERT INTO rooms (name, type, price, available)
VALUES ('Room 101', 'STANDARD', 60.00, true),
       ('Room 102', 'STANDARD', 65.00, true),
       ('Room 103', 'STANDARD', 70.00, true),
       ('Room 201', 'DELUXE', 120.00, true),
       ('Room 202', 'DELUXE', 130.00, true),
       ('Room 203', 'DELUXE', 140.00, true),
       ('Suite 301', 'SUITE', 200.00, true),
       ('Suite 302', 'SUITE', 220.00, true),
       ('Penthouse', 'SUITE', 350.00, false),
       ('Budget Room', 'STANDARD', 50.00, true);

INSERT INTO bookings (check_in_date, check_out_date, user_id, room_id)
VALUES ('2027-08-05', '2027-08-07', 1, 1),
       ('2027-08-15', '2027-08-18', 2, 2),
       ('2027-08-20', '2027-08-22', 3, 3),
       ('2027-08-28', '2027-09-02', 4, 7),
       ('2027-09-05', '2027-09-08', 5, 8),
       ('2027-10-15', '2027-10-18', 1, 4);