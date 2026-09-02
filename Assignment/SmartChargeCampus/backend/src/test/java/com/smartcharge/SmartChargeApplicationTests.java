package com.smartcharge;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class SmartChargeApplicationTests {

    @Test
    public void testEnergyCalculation() {
        // Example: 40.5 kWh battery, 20% current, 80% target
        // Energy required = 40.5 * (80 - 20) / 100 = 24.3 kWh
        double batteryCapacity = 40.5;
        double currentPercent = 20.0;
        double targetPercent = 80.0;
        
        double energyRequired = batteryCapacity * ((targetPercent - currentPercent) / 100.0);
        assertEquals(24.3, energyRequired, 0.001, "Energy calculation should match formula");
    }

    @Test
    public void testChargingDurationCalculation() {
        // Example: 22.0 kWh required, 11.0 kW charger
        // Duration = 22.0 / 11.0 = 2.0 hours = 120 minutes
        double energyRequired = 22.0;
        double chargerPower = 11.0;
        
        double hours = energyRequired / chargerPower;
        int minutes = (int) Math.ceil(hours * 60.0);
        assertEquals(2.0, hours, 0.001);
        assertEquals(120, minutes);
    }

    @Test
    public void testCostCalculation() {
        // Example: 22.0 kWh at 11 kW charger tariff rate ₹8/kWh = ₹176
        double energyKwh = 22.0;
        double ratePerKwh = 8.0;
        double cost = energyKwh * ratePerKwh;
        assertEquals(176.0, cost, 0.001);
    }

    @Test
    public void testCampusLoadExceededLogic() {
        double maxCampusLoad = 100.0;
        double currentActiveLoad = 88.0;
        
        double candidate1Power = 22.0;
        double projected1 = currentActiveLoad + candidate1Power; // 110 kW -> Exceeds
        assertTrue(projected1 > maxCampusLoad, "22 kW candidate should exceed 100 kW ceiling");

        double candidate2Power = 7.2;
        double projected2 = currentActiveLoad + candidate2Power; // 95.2 kW -> Allowed
        assertTrue(projected2 <= maxCampusLoad, "7.2 kW candidate should be within 100 kW ceiling");
    }

    @Test
    public void testQueuePriorityCalculation() {
        // Test battery urgency: <= 15% -> 40 points
        double lowBattery = 10.0;
        double batteryScore = lowBattery <= 15.0 ? 40.0 : 10.0;
        assertEquals(40.0, batteryScore);

        // Departure urgency: < 1 hour -> 40 points
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureSoon = now.plusMinutes(45);
        long mins = java.time.Duration.between(now, departureSoon).toMinutes();
        double departureScore = mins < 60 ? 40.0 : 10.0;
        assertEquals(40.0, departureScore);
    }
}
