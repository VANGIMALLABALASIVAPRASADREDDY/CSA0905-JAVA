package com.smartcharge.dao;

import com.smartcharge.exception.DatabaseOperationException;
import com.smartcharge.model.CampusLoad;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class CampusLoadDao {

    private final DataSource dataSource;
    private final double configuredMaxLoadKw;

    public CampusLoadDao(DataSource dataSource, @Value("${smartcharge.campus.max-load-kw:100.0}") double configuredMaxLoadKw) {
        this.dataSource = dataSource;
        this.configuredMaxLoadKw = configuredMaxLoadKw;
    }

    /**
     * Calculates the real live EV charging load dynamically from active charging sessions
     */
    public double getCurrentActiveLoadKw() {
        String sql = "SELECT COALESCE(SUM(p.charger_power_kw), 0.0) AS active_load " +
                     "FROM charging_sessions cs " +
                     "JOIN charging_points p ON cs.point_id = p.point_id " +
                     "WHERE cs.status = 'ACTIVE'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("active_load");
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error calculating current campus active load", e);
        }
        return 0.0;
    }

    public double getMaxCampusLoadKw() {
        String sql = "SELECT maximum_ev_load_kw FROM campus_load ORDER BY load_id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("maximum_ev_load_kw");
            }
        } catch (SQLException e) {
            // fallback to configured
        }
        return configuredMaxLoadKw;
    }

    public void recordCurrentLoad(double currentLoadKw) {
        String sql = "INSERT INTO campus_load (recorded_time, current_ev_load_kw, maximum_ev_load_kw) VALUES (NOW(), ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDouble(1, currentLoadKw);
            ps.setDouble(2, configuredMaxLoadKw);
            ps.executeUpdate();
        } catch (SQLException e) {
            // non-fatal logging
        }
    }

    public Optional<CampusLoad> getLatestCampusLoad() {
        double activeLoad = getCurrentActiveLoadKw();
        double maxLoad = getMaxCampusLoadKw();
        CampusLoad cl = new CampusLoad();
        cl.setCurrentEvLoadKw(activeLoad);
        cl.setMaximumEvLoadKw(maxLoad);
        return Optional.of(cl);
    }
}
