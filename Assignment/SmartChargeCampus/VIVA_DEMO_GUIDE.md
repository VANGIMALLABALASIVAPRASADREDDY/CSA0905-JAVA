# SmartCharge Campus — Faculty Viva & Lab Evaluation Script 🎓
### Course: CSA0905 – Programming in Java

Follow this step-by-step 28-step demonstration script during your faculty evaluation to prove that you have the complete frontend, backend, database, SQL, JDBC operations, and algorithms running locally on your laptop.

---

## 📋 PRE-DEMO CHECKLIST
1. **MySQL Server** is running on `localhost:3306`.
2. **MySQL Workbench** is open with a connection to `localhost`.
3. **IntelliJ IDEA** is open with the `SmartChargeCampus` project.

---

## 🎬 STEP-BY-STEP VIVA DEMONSTRATION

### PART 1: Database & Relational Schema (MySQL Workbench)
- **STEP 1**: In MySQL Workbench, show the `smartcharge_campus` schema under the left **Schemas** pane.
  - Run: `USE smartcharge_campus; SHOW TABLES;`
  - Show all 11 tables: `users`, `vehicles`, `charging_stations`, `charging_points`, `reservations`, `charging_sessions`, `energy_usage`, `payments`, `queue_entries`, `tariffs`, `campus_load`.
- **STEP 2**: Show Foreign Key relationships:
  - `vehicles.user_id` $\to$ `users.user_id`
  - `charging_points.station_id` $\to$ `charging_stations.station_id`
  - `reservations.point_id` $\to$ `charging_points.point_id`
  - `charging_sessions.point_id` $\to$ `charging_points.point_id`
  - `payments.session_id` $\to$ `charging_sessions.session_id`

---

### PART 2: Source Code Walkthrough in IntelliJ IDEA
- **STEP 3**: Open IntelliJ IDEA and expand the project tree.
  - Explain: *"This is a 2-tier local client-server architecture built with Maven."*
- **STEP 4**: Show Desktop Frontend (`frontend/src/main/java/com/smartcharge/client/`):
  - Point to `ui/DashboardFrame.java` (BorderLayout, CardLayout, Sidebar).
  - Point to `api/ApiClient.java` (uses standard `java.net.http.HttpClient` with JSON).
- **STEP 5**: Show Spring Boot REST Controllers (`backend/src/main/java/com/smartcharge/controller/`):
  - Show `VehicleController.java`, `RecommendationController.java`, `ChargingSessionController.java`, `ReportController.java`.
- **STEP 6**: Show Service Layer (`backend/src/main/java/com/smartcharge/service/`):
  - Show `ChargingRecommendationService.java`: Walk through the **5-Factor Scoring Algorithm** (Availability 30 pts, Wait time 25 pts, Campus load efficiency 20 pts, Location 15 pts, Departure 10 pts).
  - Show `LoadManagementService.java`: Show the **100 kW Campus EV Load ceiling enforcement**.
  - Show `QueueService.java`: Show **Priority Score calculation** ($U_{battery} + U_{departure} + U_{wait}$) and **Automatic Queue Promotion**.
- **STEP 7**: Show Plain JDBC DAO Layer (**CRITICAL CSA0905 REQUIREMENT**):
  - Open `StationDao.java`: Point directly to `Statement stmt = conn.createStatement();` used for fixed reference queries.
  - Open `VehicleDao.java` / `ReservationDao.java`: Point to `PreparedStatement ps = conn.prepareStatement(sql);` with `?` parameters for `INSERT`, `SELECT`, `UPDATE`, `DELETE`.
  - Open `ReportDao.java`: Point to `CallableStatement cs = conn.prepareCall("{CALL GetStationUtilization()}");` calling the MySQL Stored Procedure.
  - Explain: *"No Hibernate or Spring Data JPA is used for required operations. All queries are pure JDBC with Connection, Statement, PreparedStatement, CallableStatement, and ResultSet."*

---

### PART 3: Starting the Application
- **STEP 8**: Start Spring Boot Backend:
  - Run `SmartChargeApplication.java`.
  - Show console output: `Tomcat started on port 8080 (http)`.
  - Open browser to `http://localhost:8080/api/health` to show database connected.
- **STEP 9**: Start Desktop Client:
  - Run `Main.java`.
  - Show the modern FlatLaf Swing Login window.

---

### PART 4: Live Interactive Demonstration & MySQL Verification
- **STEP 10 (Login)**:
  - Click the **Student** quick-fill button (`student@campus.edu` / `student123`).
  - Click **Sign In**. The Dashboard opens.
- **STEP 11 (Dashboard Overview)**:
  - Show the live **Real-Time Campus EV Electrical Load Progress Bar** (e.g. `0 / 100 kW`).
  - Show the 8 live KPI cards populated dynamically from MySQL.
- **STEP 12 (Vehicle Registration & CRUD Demonstration)**:
  - Click **My Registered EVs** in the sidebar.
  - Click **+ Register New Vehicle**.
  - Enter:
    - Reg Number: `KA-05-EV-7777`
    - Make: `Hyundai`
    - Model: `Ioniq 5 EV`
    - Battery: `72.6` kWh
    - Connector: `CCS2`
  - Click **Save Vehicle**. Show success popup.
- **STEP 13 (Verify in MySQL Workbench)**:
  - Switch to MySQL Workbench.
  - Run: `SELECT * FROM vehicles WHERE registration_number = 'KA-05-EV-7777';`
  - Show the faculty that the vehicle was genuinely inserted into MySQL.
- **STEP 14 (Vehicle Edit & Delete)**:
  - In Swing, select the vehicle and click **Edit Vehicle**. Change battery to `75.0` kWh.
  - In Workbench, run `SELECT` again to prove the `UPDATE` worked.
- **STEP 15 (Campus Map Grid)**:
  - Click **Campus Map Grid** in the sidebar.
  - Show the 6 campus stations and point status badges (Green = AVAILABLE, Red = OCCUPIED, Orange = RESERVED, Gray = MAINTENANCE).
  - Click on any point to inspect power rating, connector type, and live status.
- **STEP 16 (Intelligent Smart Charger Recommendation)**:
  - Click **Smart Charger Finder** in the sidebar.
  - Select vehicle: `KA-01-EV-1001` (Tata Nexon EV, 40.5 kWh, CCS2).
  - Set Current Battery: `20%`.
  - Set Target Battery: `80%`.
  - Preferred Location: `Engineering Block`.
  - Expected Departure: `In 2 Hours`.
  - Click **⚡ FIND BEST CHARGER**.
  - Walk faculty through the result card:
    - Best Charger: `Engineering Block – ENG-CP01` (22 kW, CCS2).
    - Recommendation Score: `90+ / 100`.
    - Energy Required: `24.30 kWh` (calculated accurately from $40.5 \times \frac{80-20}{100}$).
    - Duration: `~67 minutes` ($24.3 / 22 \times 60$).
    - Est. Cost: `₹218.70` ($24.3 \text{ kWh} \times \text{₹9/kWh}$).
    - Projected Campus Load: `22.0 / 100.0 kW` (well within limit).
    - Match Reasons: Connector match, No schedule conflict, Within load ceiling.
- **STEP 17 (Reserve Charger)**:
  - Click **✓ RESERVE THIS CHARGER NOW**. Show reservation success popup.
  - Switch to MySQL Workbench:
    - Run: `SELECT * FROM reservations ORDER BY reservation_id DESC LIMIT 1;`
    - Show the new `CONFIRMED` reservation row.
- **STEP 18 (Reservation Conflict Detection)**:
  - In Smart Finder, try reserving the exact same charger point for the same time slot.
  - Show that the system rejects the request with a **Reservation Conflict Warning**.
- **STEP 19 (Check-In & Active Charging)**:
  - Click **Slot Reservations** in the sidebar.
  - Select the confirmed reservation and click **▶ Check-In & Start Charging**.
  - Show that reservation becomes `ACTIVE` and charging point becomes `OCCUPIED`.
  - Switch to MySQL Workbench:
    - Run: `SELECT * FROM charging_sessions WHERE status = 'ACTIVE';`
    - Run: `SELECT * FROM charging_points WHERE point_name = 'ENG-CP01';` (Status is `OCCUPIED`).
- **STEP 20 (Campus Load Dynamic Increase)**:
  - Switch to **Dashboard Overview**.
  - Show that the Campus Load bar increased by 22 kW (`22 / 100 kW`) dynamically calculated from the active session!
- **STEP 21 (Check-Out & Automatic Queue Promotion)**:
  - Click **Charging Sessions** in the sidebar.
  - Select the active session and click **⏹ End Session & Check-Out**.
  - Enter Final Battery: `80%`.
  - System completes session, calculates energy delivered, generates total cost, marks charger `AVAILABLE`, and scans virtual queue for auto-promotion!
- **STEP 22 (Payment Simulation)**:
  - Click **Pay Due Balance**.
  - Select payment method (`UPI` or `CAMPUS_WALLET`) and click **Confirm Payment**.
  - In MySQL Workbench:
    - Run: `SELECT * FROM payments ORDER BY payment_id DESC LIMIT 1;`
    - Show recorded payment amount, method, and timestamp.
- **STEP 23 (Virtual Priority Queue)**:
  - Click **Virtual Priority Queue** in the sidebar.
  - Explain priority weighting formula ($U_{battery} + U_{departure} + U_{wait}$).
- **STEP 24 (Stored Procedure Station Utilization Report)**:
  - Click **Reports & Analytics** in the sidebar.
  - In Tab 1: Show **Station Utilization Report**.
  - Explain: *"This table is populated by calling the MySQL Stored Procedure `GetStationUtilization` via JDBC `CallableStatement`."*
  - Switch to MySQL Workbench and run: `CALL GetStationUtilization();`
  - Show that the Workbench output matches the Swing UI table values exactly!
- **STEP 25 (Energy, Usage & Sustainability Analytics)**:
  - Show Tab 2: Today/Week/Month Energy and Station Distribution.
  - Show Tab 3: Peak charging periods and top charging points.
  - Show Tab 4: **Sustainability & SDGs**: Total EV Clean Energy Delivered, CO2 Emissions Avoided ($\approx 0.82\text{ kg CO}_2/\text{kWh}$ grid baseline), and UN SDG 7, 9, 11 alignment.

---

## 🎯 SUMMARY OF WHAT YOU DEMONSTRATED
1. ✅ **AWT & Swing GUI Controls, Layout Managers, Event Listeners, and FlatLaf Styling**.
2. ✅ **Pure JDBC Operations with visible `Statement`, `PreparedStatement`, `CallableStatement`, `Connection`, and `ResultSet`**.
3. ✅ **Full CRUD (Create, Read, Update, Delete) with live MySQL Workbench verification**.
4. ✅ **Intelligent 5-Factor Recommendation Engine with transparent mathematical scoring**.
5. ✅ **100 kW Campus Load Ceiling Grid Protection**.
6. ✅ **Priority Virtual Queue and Automatic Promotion**.
7. ✅ **MySQL Stored Procedure execution through `CallableStatement`**.
