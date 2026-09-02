-- ============================================================
-- SmartCharge Campus: Sample SQL Queries
-- Course: CSA0905 – Programming in Java
-- Demonstrating: INSERT, SELECT, UPDATE, DELETE, JOIN, GROUP BY, Aggregate Functions, and Stored Procedure Calls
-- ============================================================

USE smartcharge_campus;

-- 1. BASIC SELECT Queries
SELECT * FROM users;
SELECT * FROM vehicles;
SELECT * FROM charging_stations;
SELECT * FROM charging_points;
SELECT * FROM tariffs;
SELECT * FROM reservations;
SELECT * FROM charging_sessions;
SELECT * FROM payments;
SELECT * FROM queue_entries;
SELECT * FROM campus_load;

-- 2. INSERT Demonstration
-- Insert a new user
INSERT INTO users (name, email, phone, password, role) 
VALUES ('Test User', 'testuser@campus.edu', '9123456780', 'test1234', 'STUDENT');

-- Insert a vehicle for the new user
INSERT INTO vehicles (user_id, registration_number, manufacturer, model, battery_capacity_kwh, connector_type)
VALUES (LAST_INSERT_ID(), 'KA-05-EV-9999', 'Hyundai', 'Kona Electric', 39.20, 'CCS2');

-- 3. UPDATE Demonstration
-- Update vehicle battery capacity
UPDATE vehicles 
SET battery_capacity_kwh = 42.00 
WHERE registration_number = 'KA-05-EV-9999';

-- Update charging point status
UPDATE charging_points 
SET status = 'MAINTENANCE' 
WHERE point_name = 'ENG-CP03';

-- Revert charging point status
UPDATE charging_points 
SET status = 'AVAILABLE' 
WHERE point_name = 'ENG-CP03';

-- 4. DELETE Demonstration
-- Delete the test vehicle
DELETE FROM vehicles 
WHERE registration_number = 'KA-05-EV-9999';

-- Delete the test user
DELETE FROM users 
WHERE email = 'testuser@campus.edu';

-- 5. RELATIONAL JOIN Queries
-- Vehicle details with owner information
SELECT 
    v.vehicle_id,
    v.registration_number,
    v.manufacturer,
    v.model,
    v.battery_capacity_kwh,
    v.connector_type,
    u.name AS owner_name,
    u.email AS owner_email,
    u.role AS owner_role
FROM vehicles v
JOIN users u ON v.user_id = u.user_id;

-- Charging stations with their charging points and power ratings
SELECT 
    s.station_name,
    s.campus_location,
    p.point_name,
    p.charger_power_kw,
    p.connector_type,
    p.status AS charger_status,
    t.rate_per_kwh AS tariff_inr_per_kwh
FROM charging_stations s
JOIN charging_points p ON s.station_id = p.station_id
LEFT JOIN tariffs t ON p.charger_power_kw = t.charger_power_kw
ORDER BY s.station_id, p.point_id;

-- Reservation details with user, vehicle, and charging point
SELECT 
    r.reservation_id,
    u.name AS user_name,
    v.registration_number,
    v.model,
    s.station_name,
    p.point_name,
    r.start_time,
    r.end_time,
    r.status
FROM reservations r
JOIN users u ON r.user_id = u.user_id
JOIN vehicles v ON r.vehicle_id = v.vehicle_id
JOIN charging_points p ON r.point_id = p.point_id
JOIN charging_stations s ON p.station_id = s.station_id
ORDER BY r.start_time DESC;

-- Completed charging sessions with payment details
SELECT 
    cs.session_id,
    v.registration_number,
    p.point_name,
    cs.check_in_time,
    cs.check_out_time,
    cs.duration_minutes,
    cs.energy_consumed_kwh,
    cs.total_cost,
    pm.payment_method,
    pm.payment_status,
    pm.payment_time
FROM charging_sessions cs
JOIN vehicles v ON cs.vehicle_id = v.vehicle_id
JOIN charging_points p ON cs.point_id = p.point_id
LEFT JOIN payments pm ON cs.session_id = pm.session_id
ORDER BY cs.check_in_time DESC;

-- 6. AGGREGATE & GROUP BY Queries
-- Station-wise total power capacity and charger count
SELECT 
    s.station_name,
    s.campus_location,
    COUNT(p.point_id) AS total_chargers,
    SUM(p.charger_power_kw) AS total_power_capacity_kw,
    AVG(p.charger_power_kw) AS avg_power_kw
FROM charging_stations s
JOIN charging_points p ON s.station_id = p.station_id
GROUP BY s.station_id, s.station_name, s.campus_location;

-- Revenue and energy summary by connector type
SELECT 
    p.connector_type,
    COUNT(cs.session_id) AS total_sessions,
    ROUND(SUM(cs.energy_consumed_kwh), 2) AS total_kwh_delivered,
    ROUND(SUM(cs.total_cost), 2) AS total_revenue_inr
FROM charging_points p
JOIN charging_sessions cs ON p.point_id = cs.point_id
GROUP BY p.connector_type;

-- 7. STORED PROCEDURE EXECUTION (Equivalent to JDBC CallableStatement)
CALL GetStationUtilization();
CALL GenerateDailyChargingReport();
