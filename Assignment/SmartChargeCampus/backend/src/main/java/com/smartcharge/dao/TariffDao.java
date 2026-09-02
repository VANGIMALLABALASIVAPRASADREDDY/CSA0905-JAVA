package com.smartcharge.dao;

import com.smartcharge.exception.DatabaseOperationException;
import com.smartcharge.model.Tariff;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TariffDao {

    private final DataSource dataSource;

    public TariffDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Tariff> findAll() {
        List<Tariff> list = new ArrayList<>();
        String sql = "SELECT tariff_id, charger_power_kw, rate_per_kwh FROM tariffs ORDER BY charger_power_kw";
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Tariff t = new Tariff();
                t.setTariffId(rs.getInt("tariff_id"));
                t.setChargerPowerKw(rs.getDouble("charger_power_kw"));
                t.setRatePerKwh(rs.getDouble("rate_per_kwh"));
                list.add(t);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching tariffs", e);
        }
        return list;
    }

    public double getRateForPower(double powerKw) {
        String sql = "SELECT rate_per_kwh FROM tariffs WHERE charger_power_kw <= ? ORDER BY charger_power_kw DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDouble(1, powerKw);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("rate_per_kwh");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error looking up tariff for power: " + powerKw, e);
        }
        // Default fallback if not in table
        return powerKw >= 20.0 ? 9.0 : (powerKw >= 10.0 ? 8.0 : 7.0);
    }
}
