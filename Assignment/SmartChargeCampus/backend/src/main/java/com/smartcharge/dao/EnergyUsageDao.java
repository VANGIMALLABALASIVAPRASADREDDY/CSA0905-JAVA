package com.smartcharge.dao;

import com.smartcharge.exception.DatabaseOperationException;
import com.smartcharge.model.EnergyUsage;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class EnergyUsageDao {

    private final DataSource dataSource;

    public EnergyUsageDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public EnergyUsage insert(int sessionId, double energyKwh) {
        String sql = "INSERT INTO energy_usage (session_id, energy_kwh) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, sessionId);
            ps.setDouble(2, energyKwh);
            
            int affected = ps.executeUpdate();
            EnergyUsage eu = new EnergyUsage();
            eu.setSessionId(sessionId);
            eu.setEnergyKwh(energyKwh);
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        eu.setUsageId(keys.getInt(1));
                    }
                }
            }
            return eu;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error logging energy usage for session: " + sessionId, e);
        }
    }
}
