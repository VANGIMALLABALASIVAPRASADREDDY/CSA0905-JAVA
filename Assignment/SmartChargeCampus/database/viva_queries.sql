-- ============================================================
-- SmartCharge Campus: Faculty Viva Quick-Demo SQL Script
-- Course: CSA0905 – Programming in Java
-- Purpose: Run these queries during Viva evaluation to demonstrate live DB updates
-- ============================================================

USE smartcharge_campus;

-- STEP 1: Show all tables created in the schema
SHOW TABLES;

-- STEP 2: Show registered campus users
SELECT user_id, name, email, phone, role, created_at FROM users;

-- STEP 3: Show registered EV vehicles with owner relation
SELECT 
    v.vehicle_id, 
    u.name AS owner_name, 
    v.registration_number, 
    v.manufacturer, 
    v.model, 
    v.battery_capacity_kwh, 
    v.connector_type 
FROM vehicles v
JOIN users u ON v.user_id = u.user_id;

-- STEP 4: Show campus charging infrastructure (Stations & Points)
SELECT 
    s.station_id,
    s.station_name, 
    s.campus_location, 
    p.point_id,
    p.point_name, 
    p.charger_power_kw, 
    p.connector_type, 
    p.status 
FROM charging_stations s
JOIN charging_points p ON s.station_id = p.station_id
ORDER BY s.station_id, p.point_id;

-- STEP 5: Show dynamic campus electrical load status
SELECT * FROM campus_load ORDER BY recorded_time DESC LIMIT 5;

-- STEP 6: Show live reservations (Watch this after booking via UI)
SELECT 
    r.reservation_id, 
    u.name AS reserved_by, 
    v.registration_number, 
    p.point_name, 
    r.start_time, 
    r.end_time, 
    r.status, 
    r.created_at 
FROM reservations r
JOIN users u ON r.user_id = u.user_id
JOIN vehicles v ON r.vehicle_id = v.vehicle_id
JOIN charging_points p ON r.point_id = p.point_id
ORDER BY r.reservation_id DESC;

-- STEP 7: Show charging sessions (Watch this after Check-In & Check-Out via UI)
SELECT 
    cs.session_id, 
    v.registration_number, 
    p.point_name, 
    cs.check_in_time, 
    cs.check_out_time, 
    cs.starting_battery_percent, 
    cs.final_battery_percent, 
    cs.energy_consumed_kwh, 
    cs.duration_minutes, 
    cs.total_cost, 
    cs.status 
FROM charging_sessions cs
JOIN vehicles v ON cs.vehicle_id = v.vehicle_id
JOIN charging_points p ON cs.point_id = p.point_id
ORDER BY cs.session_id DESC;

-- STEP 8: Show payment transactions (Watch this after paying via UI)
SELECT 
    pm.payment_id, 
    pm.session_id, 
    pm.amount, 
    pm.payment_method, 
    pm.payment_status, 
    pm.payment_time 
FROM payments pm
ORDER BY pm.payment_id DESC;

-- STEP 9: Show virtual queue entries (Watch this when no charger is available)
SELECT 
    q.queue_id, 
    u.name AS user_name, 
    v.registration_number, 
    q.preferred_location, 
    q.current_battery_percent, 
    q.target_battery_percent, 
    q.priority_score, 
    q.queue_position, 
    q.status 
FROM queue_entries q
JOIN users u ON q.user_id = u.user_id
JOIN vehicles v ON q.vehicle_id = v.vehicle_id
ORDER BY q.priority_score DESC;

-- STEP 10: Run the Stored Procedure (Demonstrates CallableStatement backend link)
CALL GetStationUtilization();
