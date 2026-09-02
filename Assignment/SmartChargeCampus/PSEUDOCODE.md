# SmartCharge Campus — Algorithm Pseudocode Specification
### Course: CSA0905 – Programming in Java

This document specifies the formal pseudocode for all core algorithms implemented in **SmartCharge Campus**.

---

## 1. Energy Requirement Calculation
```text
ALGORITHM CalculateRequiredEnergy(batteryCapacityKwh, currentPercent, targetPercent)
INPUT:
    batteryCapacityKwh : Real (Total EV battery capacity in kWh)
    currentPercent     : Real (Starting state of charge 0..100)
    targetPercent      : Real (Desired state of charge 0..100)
OUTPUT:
    energyRequiredKwh  : Real (Energy needed to charge in kWh)

BEGIN
    IF currentPercent < 0 OR currentPercent > 100 THEN
        THROW IllegalArgumentException("Invalid current battery percentage")
    END IF
    IF targetPercent <= currentPercent OR targetPercent > 100 THEN
        THROW IllegalArgumentException("Target battery must exceed current battery")
    END IF

    percentageDiff ← (targetPercent - currentPercent) / 100.0
    energyRequiredKwh ← batteryCapacityKwh * percentageDiff

    RETURN ROUND(energyRequiredKwh, 2)
END
```

---

## 2. Estimated Charging Time & Cost Calculation
```text
ALGORITHM CalculateChargingTimeAndCost(energyRequiredKwh, chargerPowerKw, tariffRatePerKwh)
INPUT:
    energyRequiredKwh  : Real (Energy required in kWh)
    chargerPowerKw     : Real (Charger power rating in kW)
    tariffRatePerKwh   : Real (Tariff rate from tariffs table in INR/kWh)
OUTPUT:
    estimatedHours     : Real
    estimatedMinutes   : Integer
    estimatedCost      : Real

BEGIN
    estimatedHours ← energyRequiredKwh / chargerPowerKw
    estimatedMinutes ← CEILING(estimatedHours * 60)
    estimatedCost ← ROUND(energyRequiredKwh * tariffRatePerKwh, 2)

    RETURN (estimatedHours, estimatedMinutes, estimatedCost)
END
```

---

## 3. Reservation Interval Overlap Conflict Detection
```text
ALGORITHM CheckReservationConflict(pointId, requestedStart, requestedEnd, excludeResId)
INPUT:
    pointId        : Integer
    requestedStart : DateTime
    requestedEnd   : DateTime
    excludeResId   : Integer (optional, for update)
OUTPUT:
    hasConflict    : Boolean

BEGIN
    SQL ← "SELECT 1 FROM reservations 
           WHERE point_id = ? 
             AND status IN ('CONFIRMED', 'ACTIVE', 'PENDING')
             AND start_time < ? 
             AND end_time > ?"

    EXECUTE PreparedStatement WITH (pointId, requestedEnd, requestedStart)

    IF ResultSet.next() THEN
        RETURN TRUE  // Conflict exists
    ELSE
        RETURN FALSE // No overlap, slot is free
    END IF
END
```

---

## 4. Campus Electrical Load Ceiling Protection
```text
ALGORITHM ValidateCampusLoad(additionalPowerKw, maxCapacityKw)
INPUT:
    additionalPowerKw : Real (Power rating of candidate charger in kW)
    maxCapacityKw     : Real (Campus threshold, default 100.0 kW)
OUTPUT:
    isAllowed         : Boolean

BEGIN
    currentActiveLoad ← SELECT COALESCE(SUM(p.charger_power_kw), 0)
                        FROM charging_sessions cs
                        JOIN charging_points p ON cs.point_id = p.point_id
                        WHERE cs.status = 'ACTIVE'

    projectedLoad ← currentActiveLoad + additionalPowerKw

    IF projectedLoad > maxCapacityKw THEN
        RETURN FALSE // Exceeds grid safety threshold
    ELSE
        RETURN TRUE  // Safe to allocate
    END IF
END
```

---

## 5. Intelligent 5-Factor Charger Recommendation & Ranking
```text
ALGORITHM RecommendBestCharger(vehicle, currentSoC, targetSoC, prefLocation, reqStart, expDeparture)
INPUT:
    vehicle      : Vehicle (EV entity with batteryCapacity and connectorType)
    currentSoC   : Real (Current %)
    targetSoC    : Real (Target %)
    prefLocation : String (Preferred campus location)
    reqStart     : DateTime (Requested start)
    expDeparture : DateTime (Expected departure)
OUTPUT:
    bestCharger  : CandidateScoreDto or NULL (Virtual Queue recommended)

BEGIN
    energyRequired ← CalculateRequiredEnergy(vehicle.capacity, currentSoC, targetSoC)
    compatiblePoints ← SELECT * FROM charging_points WHERE connector_type = vehicle.connectorType AND status != 'INACTIVE'
    currentLoad ← GetCurrentActiveCampusLoad()
    candidatesList ← EMPTY_LIST

    FOR EACH point IN compatiblePoints DO
        (durationHours, durationMinutes, cost) ← CalculateChargingTimeAndCost(energyRequired, point.power, point.tariff)
        compTime ← reqStart + durationMinutes
        projectedLoad ← currentLoad + point.power

        eligible ← TRUE

        // Check 1: Maintenance
        IF point.status == 'MAINTENANCE' THEN
            eligible ← FALSE
        END IF

        // Check 2: Schedule Overlap
        IF CheckReservationConflict(point.id, reqStart, compTime, NULL) THEN
            eligible ← FALSE
        END IF

        // Check 3: Campus Load Constraint (<= 100 kW)
        IF projectedLoad > 100.0 THEN
            eligible ← FALSE
        END IF

        // 5-Factor Scoring (Total 100)
        score ← 0.0

        // 1. Availability Suitability (30 pts)
        IF eligible AND point.status == 'AVAILABLE' THEN
            score ← score + 30.0
        ELSE IF eligible AND point.status == 'OCCUPIED' THEN
            score ← score + 15.0
        END IF

        // 2. Waiting Time Suitability (25 pts)
        waitMins ← (point.status == 'AVAILABLE') ? 0 : 15
        score ← score + MAX(0, 25.0 - (waitMins * 0.5))

        // 3. Campus Load Efficiency (20 pts)
        IF eligible AND projectedLoad <= 80.0 THEN
            score ← score + 20.0
        ELSE IF eligible AND projectedLoad <= 100.0 THEN
            score ← score + 14.0
        END IF

        // 4. Preferred Location Suitability (15 pts)
        IF point.campusLocation == prefLocation THEN
            score ← score + 15.0
        ELSE
            score ← score + 6.0
        END IF

        // 5. Completion Before Departure (10 pts)
        IF eligible AND (expDeparture == NULL OR compTime <= expDeparture) THEN
            score ← score + 10.0
        ELSE
            score ← score + 2.0
        END IF

        candidateDto ← CreateCandidateDto(point, score, eligible, energyRequired, durationMinutes, cost, compTime, projectedLoad)
        candidatesList.ADD(candidateDto)
    END FOR

    SORT candidatesList BY totalScore DESCENDING

    bestCandidate ← FIRST eligible candidate IN candidatesList
    IF bestCandidate != NULL THEN
        RETURN bestCandidate
    ELSE
        RETURN NULL // Signal Virtual Queue Prompt
    END IF
END
```

---

## 6. Virtual Queue Priority Score & Automatic Promotion
```text
ALGORITHM CalculateQueuePriorityScore(batteryPercent, departureTime, createdTime)
INPUT:
    batteryPercent : Real (Current battery %)
    departureTime  : DateTime (Departure constraint)
    createdTime    : DateTime (Queue entry insertion timestamp)
OUTPUT:
    priorityScore  : Real

BEGIN
    // 1. Battery Urgency (10..40 pts)
    IF batteryPercent <= 15.0 THEN
        bScore ← 40.0
    ELSE IF batteryPercent <= 30.0 THEN
        bScore ← 30.0
    ELSE IF batteryPercent <= 50.0 THEN
        bScore ← 20.0
    ELSE
        bScore ← 10.0
    END IF

    // 2. Departure Urgency (10..40 pts)
    hrsLeft ← DurationBetween(NOW(), departureTime).toHours()
    IF hrsLeft < 1 THEN
        dScore ← 40.0
    ELSE IF hrsLeft <= 2 THEN
        dScore ← 30.0
    ELSE IF hrsLeft <= 4 THEN
        dScore ← 20.0
    ELSE
        dScore ← 10.0
    END IF

    // 3. Waiting Duration (+1 point per 5 minutes)
    waitMins ← DurationBetween(createdTime, NOW()).toMinutes()
    wScore ← MAX(0, waitMins / 5.0)

    RETURN ROUND(bScore + dScore + wScore, 1)
END

ALGORITHM PromoteEligibleQueuedVehicles(availablePointId)
INPUT:
    availablePointId : Integer (Newly freed charging point)
BEGIN
    point ← FindPointById(availablePointId)
    IF point.status != 'AVAILABLE' THEN RETURN

    waitingQueue ← SELECT * FROM queue_entries WHERE status = 'WAITING' ORDER BY priority_score DESC

    FOR EACH entry IN waitingQueue DO
        IF entry.connectorType == point.connectorType THEN
            IF ValidateCampusLoad(point.power, 100.0) THEN
                UPDATE queue_entries SET status = 'PROMOTED' WHERE queue_id = entry.id
                UPDATE charging_points SET status = 'RESERVED' WHERE point_id = point.id
                INSERT INTO reservations (user_id, vehicle_id, point_id, start_time, end_time, status)
                VALUES (entry.userId, entry.vehicleId, point.id, NOW(), NOW() + 60 mins, 'CONFIRMED')
                BREAK
            END IF
        END IF
    END FOR
END
```
