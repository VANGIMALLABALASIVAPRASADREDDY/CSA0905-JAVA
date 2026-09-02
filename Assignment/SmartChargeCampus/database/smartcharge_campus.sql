-- ============================================================
-- SmartCharge Campus: Database Schema & Seed Data
-- Intelligent Load-Aware E-Vehicle Charging Management System
-- Course: CSA0905 – Programming in Java
-- Database: MySQL 8.0+ / Local MySQL Server
-- ============================================================

DROP DATABASE IF EXISTS smartcharge_campus;
CREATE DATABASE smartcharge_campus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smartcharge_campus;

-- ------------------------------------------------------------
-- 1. Table: users
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'STUDENT', 'STAFF') NOT NULL DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. Table: vehicles
-- ------------------------------------------------------------
CREATE TABLE vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    registration_number VARCHAR(30) NOT NULL UNIQUE,
    manufacturer VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    battery_capacity_kwh DECIMAL(6,2) NOT NULL,
    connector_type ENUM('Type 2', 'CCS2', 'CHAdeMO', 'GB/T') NOT NULL DEFAULT 'Type 2',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicle_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. Table: charging_stations
-- ------------------------------------------------------------
CREATE TABLE charging_stations (
    station_id INT AUTO_INCREMENT PRIMARY KEY,
    station_name VARCHAR(100) NOT NULL,
    campus_location VARCHAR(100) NOT NULL,
    maximum_load_kw DECIMAL(6,2) NOT NULL DEFAULT 50.00,
    status ENUM('ACTIVE', 'MAINTENANCE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE'
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. Table: charging_points
-- ------------------------------------------------------------
CREATE TABLE charging_points (
    point_id INT AUTO_INCREMENT PRIMARY KEY,
    station_id INT NOT NULL,
    point_name VARCHAR(50) NOT NULL,
    charger_power_kw DECIMAL(5,2) NOT NULL,
    connector_type ENUM('Type 2', 'CCS2', 'CHAdeMO', 'GB/T') NOT NULL DEFAULT 'Type 2',
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    CONSTRAINT fk_point_station FOREIGN KEY (station_id) REFERENCES charging_stations(station_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 5. Table: tariffs
-- ------------------------------------------------------------
CREATE TABLE tariffs (
    tariff_id INT AUTO_INCREMENT PRIMARY KEY,
    charger_power_kw DECIMAL(5,2) NOT NULL UNIQUE,
    rate_per_kwh DECIMAL(6,2) NOT NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 6. Table: reservations
-- ------------------------------------------------------------
CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    point_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_res_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    CONSTRAINT fk_res_point FOREIGN KEY (point_id) REFERENCES charging_points(point_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 7. Table: charging_sessions
-- ------------------------------------------------------------
CREATE TABLE charging_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NULL,
    vehicle_id INT NOT NULL,
    point_id INT NOT NULL,
    check_in_time DATETIME NOT NULL,
    check_out_time DATETIME NULL,
    starting_battery_percent DECIMAL(5,2) NOT NULL,
    target_battery_percent DECIMAL(5,2) NOT NULL,
    final_battery_percent DECIMAL(5,2) NULL,
    energy_consumed_kwh DECIMAL(7,2) DEFAULT 0.00,
    duration_minutes INT DEFAULT 0,
    total_cost DECIMAL(8,2) DEFAULT 0.00,
    status ENUM('ACTIVE', 'COMPLETED', 'TERMINATED') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_session_res FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE SET NULL,
    CONSTRAINT fk_session_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    CONSTRAINT fk_session_point FOREIGN KEY (point_id) REFERENCES charging_points(point_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 8. Table: energy_usage
-- ------------------------------------------------------------
CREATE TABLE energy_usage (
    usage_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    energy_kwh DECIMAL(7,2) NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usage_session FOREIGN KEY (session_id) REFERENCES charging_sessions(session_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 9. Table: payments
-- ------------------------------------------------------------
CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    amount DECIMAL(8,2) NOT NULL,
    payment_method ENUM('UPI', 'CARD', 'CAMPUS_WALLET', 'CASH') NOT NULL DEFAULT 'UPI',
    payment_status ENUM('PAID', 'PENDING', 'FAILED') NOT NULL DEFAULT 'PAID',
    payment_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_session FOREIGN KEY (session_id) REFERENCES charging_sessions(session_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 10. Table: queue_entries
-- ------------------------------------------------------------
CREATE TABLE queue_entries (
    queue_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    preferred_location VARCHAR(100) NOT NULL,
    current_battery_percent DECIMAL(5,2) NOT NULL,
    target_battery_percent DECIMAL(5,2) NOT NULL,
    requested_time DATETIME NOT NULL,
    departure_time DATETIME NOT NULL,
    priority_score DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    queue_position INT NOT NULL DEFAULT 1,
    status ENUM('WAITING', 'PROMOTED', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'WAITING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_queue_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_queue_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 11. Table: campus_load
-- ------------------------------------------------------------
CREATE TABLE campus_load (
    load_id INT AUTO_INCREMENT PRIMARY KEY,
    recorded_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    current_ev_load_kw DECIMAL(6,2) NOT NULL DEFAULT 0.00,
    maximum_ev_load_kw DECIMAL(6,2) NOT NULL DEFAULT 100.00
) ENGINE=InnoDB;

-- ============================================================
-- STORED PROCEDURES (FOR JDBC CallableStatement DEMONSTRATION)
-- ============================================================

DELIMITER //

CREATE PROCEDURE GetStationUtilization()
BEGIN
    SELECT 
        s.station_id,
        s.station_name,
        s.campus_location,
        s.maximum_load_kw,
        COUNT(DISTINCT p.point_id) AS total_points,
        SUM(CASE WHEN p.status = 'OCCUPIED' THEN 1 ELSE 0 END) AS active_points,
        COUNT(DISTINCT cs.session_id) AS total_sessions,
        COALESCE(SUM(cs.energy_consumed_kwh), 0.00) AS total_energy_kwh,
        COALESCE(SUM(cs.total_cost), 0.00) AS total_revenue,
        ROUND(
            CASE 
                WHEN COUNT(DISTINCT p.point_id) = 0 THEN 0.0
                ELSE (SUM(CASE WHEN p.status = 'OCCUPIED' THEN 1 ELSE 0 END) * 100.0) / COUNT(DISTINCT p.point_id)
            END, 2
        ) AS utilization_percent
    FROM charging_stations s
    LEFT JOIN charging_points p ON s.station_id = p.station_id
    LEFT JOIN charging_sessions cs ON p.point_id = cs.point_id
    GROUP BY s.station_id, s.station_name, s.campus_location, s.maximum_load_kw
    ORDER BY s.station_id;
END //

CREATE PROCEDURE GenerateDailyChargingReport()
BEGIN
    SELECT 
        DATE(cs.check_in_time) AS report_date,
        COUNT(cs.session_id) AS total_sessions,
        ROUND(COALESCE(SUM(cs.energy_consumed_kwh), 0), 2) AS total_energy_kwh,
        ROUND(COALESCE(AVG(cs.duration_minutes), 0), 1) AS avg_duration_mins,
        ROUND(COALESCE(SUM(cs.total_cost), 0), 2) AS total_revenue
    FROM charging_sessions cs
    WHERE cs.status = 'COMPLETED'
    GROUP BY DATE(cs.check_in_time)
    ORDER BY report_date DESC;
END //

DELIMITER ;

-- ============================================================
-- SEED DATA (REALISTIC CAMPUS CHARGING NETWORK)
-- ============================================================

-- 1. Users (Credentials: admin@campus.edu / admin123, student@campus.edu / student123, etc.)
INSERT INTO users (user_id, name, email, phone, password, role) VALUES
(1, 'Campus Administrator', 'admin@campus.edu', '9876543210', 'admin123', 'ADMIN'),
(2, 'Aarav Sharma (Student)', 'student@campus.edu', '9876543211', 'student123', 'STUDENT'),
(3, 'Dr. Priya Nair (Staff)', 'staff@campus.edu', '9876543212', 'staff123', 'STAFF'),
(4, 'Rohan Mehta (Student)', 'rohan@campus.edu', '9876543213', 'rohan123', 'STUDENT'),
(5, 'Prof. S. Rao (Staff)', 'rao@campus.edu', '9876543214', 'rao123', 'STAFF');

-- 2. Vehicles
INSERT INTO vehicles (vehicle_id, user_id, registration_number, manufacturer, model, battery_capacity_kwh, connector_type) VALUES
(1, 2, 'KA-01-EV-1001', 'Tata', 'Nexon EV MAX', 40.50, 'CCS2'),
(2, 2, 'KA-01-EV-1002', 'Ather', '450X Gen 3', 3.70, 'Type 2'),
(3, 3, 'KA-05-EV-2001', 'MG', 'ZS EV Exclusive', 50.30, 'CCS2'),
(4, 4, 'KA-03-EV-3001', 'Ola', 'S1 Pro Gen 2', 4.00, 'Type 2'),
(5, 5, 'KA-04-EV-4001', 'Hyundai', 'Ioniq 5', 72.60, 'CCS2'),
(6, 1, 'KA-01-ADM-0001', 'Mahindra', 'XUV400 EL', 39.40, 'CCS2');

-- 3. Charging Stations (6 Realistic Campus Locations)
INSERT INTO charging_stations (station_id, station_name, campus_location, maximum_load_kw, status) VALUES
(1, 'Engineering Block Station', 'Engineering Block', 45.00, 'ACTIVE'),
(2, 'Main Administrative Block', 'Main Block', 35.00, 'ACTIVE'),
(3, 'Central Library Plaza', 'Library', 30.00, 'ACTIVE'),
(4, 'Boys Hostel North Hub', 'Boys Hostel', 25.00, 'ACTIVE'),
(5, 'Girls Hostel South Hub', 'Girls Hostel', 25.00, 'ACTIVE'),
(6, 'Main Campus Visitors Parking', 'Main Parking', 50.00, 'ACTIVE');

-- 4. Charging Points (14 Points: 7 kW, 11 kW, 22 kW with Type 2 and CCS2)
INSERT INTO charging_points (point_id, station_id, point_name, charger_power_kw, connector_type, status) VALUES
(1, 1, 'ENG-CP01', 22.00, 'CCS2', 'AVAILABLE'),
(2, 1, 'ENG-CP02', 11.00, 'Type 2', 'AVAILABLE'),
(3, 1, 'ENG-CP03', 7.20, 'Type 2', 'AVAILABLE'),
(4, 2, 'MB-CP01', 22.00, 'CCS2', 'AVAILABLE'),
(5, 2, 'MB-CP02', 11.00, 'Type 2', 'AVAILABLE'),
(6, 3, 'LIB-CP01', 11.00, 'CCS2', 'AVAILABLE'),
(7, 3, 'LIB-CP02', 7.20, 'Type 2', 'AVAILABLE'),
(8, 4, 'BH-CP01', 7.20, 'Type 2', 'AVAILABLE'),
(9, 4, 'BH-CP02', 7.20, 'Type 2', 'AVAILABLE'),
(10, 5, 'GH-CP01', 7.20, 'Type 2', 'AVAILABLE'),
(11, 5, 'GH-CP02', 7.20, 'Type 2', 'AVAILABLE'),
(12, 6, 'PKG-CP01', 22.00, 'CCS2', 'AVAILABLE'),
(13, 6, 'PKG-CP02', 22.00, 'CCS2', 'AVAILABLE'),
(14, 6, 'PKG-CP03', 11.00, 'Type 2', 'MAINTENANCE');

-- 5. Tariffs (Realistic standard rates in INR/kWh)
INSERT INTO tariffs (tariff_id, charger_power_kw, rate_per_kwh) VALUES
(1, 7.20, 7.00),
(2, 11.00, 8.00),
(3, 22.00, 9.00);

-- 6. Initial Campus Load Tracker (Max Capacity 100 kW)
INSERT INTO campus_load (load_id, recorded_time, current_ev_load_kw, maximum_ev_load_kw) VALUES
(1, NOW(), 0.00, 100.00);

-- 7. Seed Sample Historical Completed Reservations, Sessions, Usage, and Payments
INSERT INTO reservations (reservation_id, user_id, vehicle_id, point_id, start_time, end_time, status, created_at) VALUES
(1, 3, 3, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 22 HOUR), 'COMPLETED', DATE_SUB(NOW(), INTERVAL 26 HOUR));

INSERT INTO charging_sessions (session_id, reservation_id, vehicle_id, point_id, check_in_time, check_out_time, starting_battery_percent, target_battery_percent, final_battery_percent, energy_consumed_kwh, duration_minutes, total_cost, status) VALUES
(1, 1, 3, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 22 HOUR), 20.00, 80.00, 80.00, 30.18, 120, 271.62, 'COMPLETED');

INSERT INTO energy_usage (usage_id, session_id, energy_kwh, recorded_at) VALUES
(1, 1, 30.18, DATE_SUB(NOW(), INTERVAL 22 HOUR));

INSERT INTO payments (payment_id, session_id, amount, payment_method, payment_status, payment_time) VALUES
(1, 1, 271.62, 'CAMPUS_WALLET', 'PAID', DATE_SUB(NOW(), INTERVAL 22 HOUR));

INSERT INTO reservations (reservation_id, user_id, vehicle_id, point_id, start_time, end_time, status, created_at) VALUES
(2, 4, 4, 8, DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR), 'COMPLETED', DATE_SUB(NOW(), INTERVAL 8 HOUR));

INSERT INTO charging_sessions (session_id, reservation_id, vehicle_id, point_id, check_in_time, check_out_time, starting_battery_percent, target_battery_percent, final_battery_percent, energy_consumed_kwh, duration_minutes, total_cost, status) VALUES
(2, 2, 4, 8, DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 5 HOUR), 30.00, 90.00, 90.00, 2.40, 60, 16.80, 'COMPLETED');

INSERT INTO energy_usage (usage_id, session_id, energy_kwh, recorded_at) VALUES
(2, 2, 2.40, DATE_SUB(NOW(), INTERVAL 5 HOUR));

INSERT INTO payments (payment_id, session_id, amount, payment_method, payment_status, payment_time) VALUES
(2, 2, 16.80, 'UPI', 'PAID', DATE_SUB(NOW(), INTERVAL 5 HOUR));
