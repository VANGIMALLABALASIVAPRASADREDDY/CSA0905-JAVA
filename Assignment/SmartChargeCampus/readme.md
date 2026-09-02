# SmartCharge Campus ⚡
### Intelligent Load-Aware E-Vehicle Charging Management System for a Smart University Campus

**Course**: CSA0905 – Programming in Java  
**Architecture**: Java Swing/AWT Desktop GUI $\to$ REST API (JSON) $\to$ Spring Boot Backend $\to$ Pure JDBC DAO Layer $\to$ Local MySQL Database Server  
**Target Platform**: Local Windows System with IntelliJ IDEA & MySQL Workbench

---

## 📌 1. Project Description & Academic Context

**SmartCharge Campus** is an intelligent electric vehicle charging resource scheduling and load-management platform built specifically for university environments. Unlike conventional charging systems that only display static charger occupancy, SmartCharge Campus implements real-time campus power grid protection (100 kW load ceiling), 5-factor deterministic multi-criteria charger recommendation, and an urgency-weighted virtual queue with automatic vehicle promotion.

### Critical CSA0905 Academic Evaluation Requirements
This project strictly demonstrates all required core Java concepts without hiding them behind object-relational mapping frameworks:
- **AWT & Swing**: Custom frames (`LoginFrame`, `DashboardFrame`), `BorderLayout`, `CardLayout`, `GridLayout`, `BoxLayout`, `JProgressBar`, `JTable`, `JTabbedPane`, `JSlider`, `JSpinner`, custom cell renderers, and event listeners.
- **Pure JDBC Operations (NO Spring Data JPA / Hibernate)**:
  - `java.sql.Connection` & `DataSource` connection pooling.
  - `java.sql.Statement`: Demonstrated in [`StationDao.java`](file:///d:/College/Java/Assignment/SmartChargeCampus/backend/src/main/java/com/smartcharge/dao/StationDao.java) for fixed reference queries (`findAllUsingStatement`).
  - `java.sql.PreparedStatement`: Extensively used across all DAOs with `?` parameter substitution for `INSERT`, `SELECT`, `UPDATE`, and `DELETE`.
  - `java.sql.CallableStatement`: Demonstrated in [`ReportDao.java`](file:///d:/College/Java/Assignment/SmartChargeCampus/backend/src/main/java/com/smartcharge/dao/ReportDao.java) calling the MySQL Stored Procedure `GetStationUtilization()`.
  - `java.sql.ResultSet`: Explicit column extraction and DTO mapping.
- **Relational Integrity**: 11 normalized MySQL tables linked via foreign keys and constraints.
- **Clean Architecture & REST**: Standard DTOs, custom exception handling with `@ControllerAdvice`, and `java.net.http.HttpClient` desktop client.

---

## 🏗 2. Final System Architecture

```
┌────────────────────────────────────────────────────────┐
│             JAVA SWING / AWT DESKTOP FRONTEND          │
│   DashboardFrame | CampusMap | SmartFinder | JTable    │
└───────────────────────────┬────────────────────────────┘
                            │ HTTP REST / JSON (java.net.http.HttpClient)
                            ▼
┌────────────────────────────────────────────────────────┐
│             SPRING BOOT REST BACKEND (:8080)           │
│   Auth | Vehicle | Station | Queue | Session | Reports │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│             SERVICE LAYER & SMART ENGINES              │
│   • ChargingRecommendationService (5-Factor Engine)    │
│   • LoadManagementService (100 kW Campus Protection)   │
│   • QueueService (Priority Scoring & Auto-Promotion)   │
└───────────────────────────┬────────────────────────────┘
                            │ Plain JDBC Calls
                            ▼
┌────────────────────────────────────────────────────────┐
│             DAO / REPOSITORY LAYER                     │
│   Statement | PreparedStatement | CallableStatement    │
└───────────────────────────┬────────────────────────────┘
                            │ Local TCP Port 3306
                            ▼
┌────────────────────────────────────────────────────────┐
│             LOCAL MYSQL DATABASE SERVER                │
│   Schema: smartcharge_campus (11 Relational Tables)    │
└────────────────────────────────────────────────────────┘
```

---

## 🗄 3. Database Design & Tables (`smartcharge_campus`)

The database consists of 11 relational tables:
1. `users`: Stores campus users (`ADMIN`, `STUDENT`, `STAFF`).
2. `vehicles`: Registered EVs linked to `users.user_id` with battery capacity and connector types (`Type 2`, `CCS2`, `CHAdeMO`, `GB/T`).
3. `charging_stations`: 6 campus charging hubs (Engineering Block, Main Block, Library, Boys Hostel, Girls Hostel, Main Parking).
4. `charging_points`: 14 physical chargers (7.2 kW, 11 kW, 22 kW) with real-time status (`AVAILABLE`, `OCCUPIED`, `RESERVED`, `MAINTENANCE`).
5. `tariffs`: Power-based pricing (7.2 kW = ₹7/kWh, 11 kW = ₹8/kWh, 22 kW = ₹9/kWh).
6. `reservations`: Slot reservations with start/end intervals and state transitions.
7. `charging_sessions`: Active & historical sessions tracking starting/final battery %, energy delivered (kWh), duration (mins), and total cost (₹).
8. `energy_usage`: Time-series logging of delivered electrical energy.
9. `payments`: Simulated payment settlement (`UPI`, `CARD`, `CAMPUS_WALLET`, `CASH`).
10. `queue_entries`: Virtual queue entries ranked by priority score.
11. `campus_load`: Dynamic campus charging load tracker (maximum limit: 100 kW).

### Stored Procedure (`GetStationUtilization`)
```sql
CREATE PROCEDURE GetStationUtilization()
BEGIN
    SELECT 
        s.station_id, s.station_name, s.campus_location, s.maximum_load_kw,
        COUNT(DISTINCT p.point_id) AS total_points,
        SUM(CASE WHEN p.status = 'OCCUPIED' THEN 1 ELSE 0 END) AS active_points,
        COUNT(DISTINCT cs.session_id) AS total_sessions,
        COALESCE(SUM(cs.energy_consumed_kwh), 0.00) AS total_energy_kwh,
        COALESCE(SUM(cs.total_cost), 0.00) AS total_revenue,
        ROUND(CASE WHEN COUNT(DISTINCT p.point_id) = 0 THEN 0.0
                   ELSE (SUM(CASE WHEN p.status = 'OCCUPIED' THEN 1 ELSE 0 END) * 100.0) / COUNT(DISTINCT p.point_id)
              END, 2) AS utilization_percent
    FROM charging_stations s
    LEFT JOIN charging_points p ON s.station_id = p.station_id
    LEFT JOIN charging_sessions cs ON p.point_id = cs.point_id
    GROUP BY s.station_id, s.station_name, s.campus_location, s.maximum_load_kw;
END;
```

---

## ⚡ 4. Intelligent Smart Recommendation Algorithm

Our standout feature calculates the optimal charger using a transparent 5-factor scoring engine:

$$\text{Total Score (100 pts)} = S_{\text{avail}} (30) + S_{\text{wait}} (25) + S_{\text{load}} (20) + S_{\text{loc}} (15) + S_{\text{dep}} (10)$$

1. **Required Energy ($E$)**:
   $$E = \text{Battery Capacity (kWh)} \times \frac{\text{Target \%} - \text{Current \%}}{100}$$
2. **Estimated Charging Duration**:
   $$\text{Hours} = \frac{E}{\text{Charger Power (kW)}}, \quad \text{Minutes} = \lceil \text{Hours} \times 60 \rceil$$
3. **Estimated Cost**:
   $$\text{Cost (₹)} = E \times \text{Tariff Rate (₹/kWh)}$$
4. **Campus Load Validation**:
   $$\text{Projected Load} = \text{Current Active Load} + P_{\text{candidate}} \le 100\text{ kW}$$
5. **Interval Overlap Conflict Check**:
   $$\text{Conflict} \iff (T_{\text{start}} < T_{\text{existing\_end}}) \land (T_{\text{end}} > T_{\text{existing\_start}})$$

### Virtual Queue Priority Score:
$$\text{Priority Score} = U_{\text{battery}} (10-40\text{ pts}) + U_{\text{departure}} (10-40\text{ pts}) + U_{\text{wait}} (+1\text{ pt / 5 mins})$$

---

## 🚀 5. Quick Setup & Execution Guide

### Step 1: MySQL Workbench Setup
1. Open **MySQL Workbench** and connect to your local MySQL instance (`localhost:3306`).
2. Open `database/smartcharge_campus.sql`.
3. Click the **Execute (Lightning bolt)** button.
4. Refresh the Schemas pane and verify that `smartcharge_campus` and its 11 tables appear.

### Step 2: Open Project in IntelliJ IDEA
1. Open IntelliJ IDEA.
2. Select **File $\to$ Open...** and choose the `SmartChargeCampus` folder.
3. IntelliJ will automatically detect the root Maven `pom.xml` and download dependencies.

### Step 3: Configure Database Password (if different from default)
If your local MySQL root password is not `root`, either:
- Set environment variable `DB_PASSWORD=your_password`, OR
- Edit `backend/src/main/resources/application.properties`:
  ```properties
  spring.datasource.password=your_password
  ```

### Step 4: Run Spring Boot Backend
1. In IntelliJ, navigate to:  
   `backend/src/main/java/com/smartcharge/SmartChargeApplication.java`
2. Right-click and select **Run 'SmartChargeApplication'**.
3. Verify the console displays:  
   `Tomcat started on port 8080 (http)`
4. Health check: open `http://localhost:8080/api/health` in browser to verify DB connection.

### Step 5: Run Java Swing Desktop Frontend
1. In IntelliJ, navigate to:  
   `frontend/src/main/java/com/smartcharge/client/Main.java`
2. Right-click and select **Run 'Main'**.
3. The modern Swing Login window will appear!

---

## 🔑 6. Demo User Credentials

| Role | Email | Password | Pre-seeded EV |
| :--- | :--- | :--- | :--- |
| **Student** | `student@campus.edu` | `student123` | Tata Nexon EV MAX (`KA-01-EV-1001`), Ather 450X (`KA-01-EV-1002`) |
| **Admin** | `admin@campus.edu` | `admin123` | Mahindra XUV400 (`KA-01-ADM-0001`) + Admin Tabs |
| **Staff** | `staff@campus.edu` | `staff123` | MG ZS EV Exclusive (`KA-05-EV-2001`) |

*(Tip: You can also click the quick demo buttons on the login screen to autofill credentials!)*

---

## 📁 7. Project Structure

```
SmartChargeCampus/
├── pom.xml                                   # Root Maven aggregator POM
│
├── backend/
│   ├── pom.xml                               # Backend dependencies (Spring Boot, MySQL, JDBC)
│   └── src/
│       ├── main/
│       │   ├── java/com/smartcharge/
│       │   │   ├── SmartChargeApplication.java
│       │   │   ├── config/WebConfig.java
│       │   │   ├── controller/               # 12 REST Controllers
│       │   │   ├── service/                  # Business logic & algorithms
│       │   │   ├── dao/                      # Plain JDBC DAOs (Statement, PreparedStatement, CallableStatement)
│       │   │   ├── model/                    # Domain entities
│       │   │   ├── dto/                      # Transfer objects
│       │   │   └── exception/                # Custom exceptions & @ControllerAdvice
│       │   └── resources/application.properties
│       └── test/java/com/smartcharge/SmartChargeApplicationTests.java
│
├── frontend/
│   ├── pom.xml                               # Frontend dependencies (FlatLaf, Jackson)
│   └── src/main/java/com/smartcharge/client/
│       ├── Main.java                         # Swing application entrypoint
│       ├── api/ApiClient.java                # HTTP REST client
│       ├── model/                            # Client DTOs
│       ├── ui/                               # Modern Swing UI Frames & Panels
│       └── util/                             # UIUtils & SessionContext
│
├── database/
│   ├── smartcharge_campus.sql                # Complete DDL schema & seed data
│   ├── sample_queries.sql                    # CRUD, Joins, Aggregates & Procedure Calls
│   └── viva_queries.sql                      # Faculty viva demonstration queries
│
├── README.md                                 # Complete documentation
├── VIVA_DEMO_GUIDE.md                        # Step-by-step viva presentation script
├── PSEUDOCODE.md                             # Algorithm pseudocode
├── UNIQUE_FEATURES.md                        # Innovation & SDG analysis
└── .gitignore
```
