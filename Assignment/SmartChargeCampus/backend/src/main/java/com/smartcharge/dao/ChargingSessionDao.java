package com.smartcharge.dao;

import com.smartcharge.exception.DatabaseOperationException;
import com.smartcharge.model.ChargingSession;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ChargingSessionDao {

    private final DataSource dataSource;

    public ChargingSessionDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ChargingSession insert(ChargingSession cs) {
        String sql = "INSERT INTO charging_sessions (reservation_id, vehicle_id, point_id, check_in_time, " +
                     "starting_battery_percent, target_battery_percent, energy_consumed_kwh, duration_minutes, total_cost, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (cs.getReservationId() != null) {
                ps.setInt(1, cs.getReservationId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, cs.getVehicleId());
            ps.setInt(3, cs.getPointId());
            ps.setTimestamp(4, Timestamp.valueOf(cs.getCheckInTime()));
            ps.setDouble(5, cs.getStartingBatteryPercent());
            ps.setDouble(6, cs.getTargetBatteryPercent());
            ps.setDouble(7, cs.getEnergyConsumedKwh());
            ps.setInt(8, cs.getDurationMinutes());
            ps.setDouble(9, cs.getTotalCost());
            ps.setString(10, cs.getStatus() != null ? cs.getStatus() : "ACTIVE");
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        cs.setSessionId(keys.getInt(1));
                    }
                }
            }
            return cs;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error starting charging session", e);
        }
    }

    public boolean completeSession(int sessionId, LocalDateTime checkOutTime, double finalBatteryPercent, 
                                   double energyConsumedKwh, int durationMinutes, double totalCost) {
        String sql = "UPDATE charging_sessions SET check_out_time = ?, final_battery_percent = ?, " +
                     "energy_consumed_kwh = ?, duration_minutes = ?, total_cost = ?, status = 'COMPLETED' " +
                     "WHERE session_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, Timestamp.valueOf(checkOutTime));
            ps.setDouble(2, finalBatteryPercent);
            ps.setDouble(3, energyConsumedKwh);
            ps.setInt(4, durationMinutes);
            ps.setDouble(5, totalCost);
            ps.setInt(6, sessionId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error completing charging session ID: " + sessionId, e);
        }
    }

    public Optional<ChargingSession> findById(int sessionId) {
        String sql = baseQuery() + "WHERE cs.session_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSession(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding session ID: " + sessionId, e);
        }
        return Optional.empty();
    }

    public List<ChargingSession> findActiveSessions() {
        List<ChargingSession> list = new ArrayList<>();
        String sql = baseQuery() + "WHERE cs.status = 'ACTIVE' ORDER BY cs.check_in_time DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapSession(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching active sessions", e);
        }
        return list;
    }

    public List<ChargingSession> findAll() {
        List<ChargingSession> list = new ArrayList<>();
        String sql = baseQuery() + "ORDER BY cs.session_id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapSession(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all sessions", e);
        }
        return list;
    }

    public List<ChargingSession> findByUserId(int userId) {
        List<ChargingSession> list = new ArrayList<>();
        String sql = baseQuery() + "WHERE v.user_id = ? ORDER BY cs.session_id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSession(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching sessions for user: " + userId, e);
        }
        return list;
    }

    public Optional<ChargingSession> findActiveSessionForPoint(int pointId) {
        String sql = baseQuery() + "WHERE cs.point_id = ? AND cs.status = 'ACTIVE' LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, pointId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSession(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding active session for point: " + pointId, e);
        }
        return Optional.empty();
    }

    private String baseQuery() {
        return "SELECT cs.session_id, cs.reservation_id, cs.vehicle_id, cs.point_id, cs.check_in_time, " +
               "cs.check_out_time, cs.starting_battery_percent, cs.target_battery_percent, cs.final_battery_percent, " +
               "cs.energy_consumed_kwh, cs.duration_minutes, cs.total_cost, cs.status, " +
               "v.registration_number, v.model AS vehicle_model, p.point_name, p.charger_power_kw, " +
               "s.station_name, s.campus_location, u.name AS user_name " +
               "FROM charging_sessions cs " +
               "JOIN vehicles v ON cs.vehicle_id = v.vehicle_id " +
               "JOIN users u ON v.user_id = u.user_id " +
               "JOIN charging_points p ON cs.point_id = p.point_id " +
               "JOIN charging_stations s ON p.station_id = s.station_id ";
    }

    private ChargingSession mapSession(ResultSet rs) throws SQLException {
        ChargingSession cs = new ChargingSession();
        cs.setSessionId(rs.getInt("session_id"));
        int resId = rs.getInt("reservation_id");
        if (!rs.wasNull()) {
            cs.setReservationId(resId);
        }
        cs.setVehicleId(rs.getInt("vehicle_id"));
        cs.setPointId(rs.getInt("point_id"));
        
        Timestamp in = rs.getTimestamp("check_in_time");
        if (in != null) cs.setCheckInTime(in.toLocalDateTime());
        
        Timestamp out = rs.getTimestamp("check_out_time");
        if (out != null) cs.setCheckOutTime(out.toLocalDateTime());

        cs.setStartingBatteryPercent(rs.getDouble("starting_battery_percent"));
        cs.setTargetBatteryPercent(rs.getDouble("target_battery_percent"));
        
        double fin = rs.getDouble("final_battery_percent");
        if (!rs.wasNull()) {
            cs.setFinalBatteryPercent(fin);
        }

        cs.setEnergyConsumedKwh(rs.getDouble("energy_consumed_kwh"));
        cs.setDurationMinutes(rs.getInt("duration_minutes"));
        cs.setTotalCost(rs.getDouble("total_cost"));
        cs.setStatus(rs.getString("status"));

        cs.setRegistrationNumber(rs.getString("registration_number"));
        cs.setVehicleModel(rs.getString("vehicle_model"));
        cs.setPointName(rs.getString("point_name"));
        cs.setStationName(rs.getString("station_name"));
        cs.setCampusLocation(rs.getString("campus_location"));
        cs.setChargerPowerKw(rs.getDouble("charger_power_kw"));
        cs.setUserName(rs.getString("user_name"));
        return cs;
    }
}
