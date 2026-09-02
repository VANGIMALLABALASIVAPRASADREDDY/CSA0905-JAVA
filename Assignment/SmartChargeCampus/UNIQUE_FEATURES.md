# SmartCharge Campus — Standout Innovations & SDG Alignment

---

## 🌟 1. Conventional EV Charging Apps vs. SmartCharge Campus

| Feature | Conventional EV Booking App | SmartCharge Campus (Our System) |
| :--- | :--- | :--- |
| **Charger Allocation** | Static "first-come first-served" list of green pins | **Intelligent 5-factor multi-criteria ranking** taking vehicle battery capacity, connector compatibility, departure constraints, and campus electrical grid state into account. |
| **Campus Grid Load Awareness** | None. Blindly allows concurrent high-power charging, risking transformer trip or substation failure. | **Active Campus Load Management Engine (100 kW safety ceiling)** computed dynamically from live active sessions. High-power allocations are prevented if grid headroom is insufficient. |
| **Handling Congestion / Full Capacity** | Shows "No chargers available" error; user must repeatedly retry. | **Priority-based Virtual Queue**: Ranks waiting EVs using deterministic battery urgency, departure urgency, and queue waiting bonuses, automatically promoting vehicles when a session checks out. |
| **Pricing & Cost Estimation** | Flat rate or manual calculation after charging. | **Dynamic Power-Tiered Tariff System (₹7/₹8/₹9 per kWh)** with instantaneous cost preview calculated before booking. |
| **Enterprise Persistence Architecture** | High-level ORMs hiding raw queries. | **Transparent Pure JDBC DAO architecture** (`Statement`, `PreparedStatement`, `CallableStatement`) with zero ORM abstraction overhead. |

---

## 💡 2. The 5-Factor Deterministic Recommendation Engine

Instead of relying on opaque machine learning, SmartCharge Campus implements a transparent, deterministic scoring model out of 100 points:

1. **Availability Suitability (30 Pts)**: Immediate access without time slot overlap or maintenance downtime.
2. **Waiting Time Minimization (25 Pts)**: Penalizes points with estimated wait times or ongoing charging.
3. **Campus Load Efficiency (20 Pts)**: Rewards charging points that operate comfortably within the campus 100 kW electrical ceiling.
4. **Preferred Location Proximity (15 Pts)**: Matches the user's specific campus zone (e.g. Engineering Block vs Library).
5. **Departure Constraint Compliance (10 Pts)**: Validates that the charging cycle completes before the user's class or work shift ends.

---

## ⚡ 3. Real-Time Campus Load Protection

The campus electrical distribution substation has a dedicated 100 kW EV charging budget. When multiple fast chargers (22 kW) are connected simultaneously, unrestrained allocation can cause localized brownouts.
- **SmartCharge Campus** dynamically aggregates active charging power:
  $$\text{Active Load} = \sum_{i \in \text{Active Sessions}} P_{\text{point } i}$$
- If a user requests a 22 kW charger while current load is 88 kW, the system recognizes that $88 + 22 = 110\text{ kW} > 100\text{ kW}$ and either recommends an 11 kW charger or defers the request to the virtual queue.

---

## 🌍 4. Alignment with UN Sustainable Development Goals (SDGs)

### 🟢 SDG 7: Affordable and Clean Energy
- By scheduling charging according to power levels and university tariff tiers (₹7/kWh to ₹9/kWh), students and faculty receive affordable charging.
- Smart load leveling eliminates sudden current spikes, enhancing the lifespan of university energy infrastructure.

### 🏢 SDG 9: Industry, Innovation and Infrastructure
- Transforms standard campus parking lots into an intelligent, connected EV charging ecosystem.
- Real-time station utilization reports generated via MySQL stored procedures enable university infrastructure planners to pinpoint high-demand hubs.

### 🏙 SDG 11: Sustainable Cities and Communities
- Encourages student and faculty transition from internal combustion engine (ICE) vehicles to zero-emission EVs.
- Prevents charging queue congestion and eliminates campus parking clutter through virtual queuing.
- Real-time sustainability metrics calculate avoided CO2 emissions using standard baseline conversion factors ($0.82\text{ kg CO}_2\text{ avoided / kWh}$).
