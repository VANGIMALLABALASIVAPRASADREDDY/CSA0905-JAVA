package com.smartcharge.dao;

import com.smartcharge.exception.DatabaseOperationException;
import com.smartcharge.model.Reservation;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationDao {

    private final DataSource dataSource;

    public ReservationDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Checks if a reservation conflict exists for the given point in the time interval.
     * Overlap condition: (requestedStart < existing.end_time) AND (requestedEnd > existing.start_time)
     */
    public boolean hasConflict(int pointId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeReservationId) {
        StringBuilder sql = new StringBuilder(
            "SELECT 1 FROM reservations " +
            "WHERE point_id = ? " +
            "AND status IN ('CONFIRMED', 'ACTIVE', 'PENDING') " +
            "AND start_time < ? " +
            "AND end_time > ? "
        );

        if (excludeReservationId != null) {
            sql.append("AND reservation_id != ? ");
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            ps.setInt(1, pointId);
            ps.setTimestamp(2, Timestamp.valueOf(endTime));
            ps.setTimestamp(3, Timestamp.valueOf(startTime));
            
            if (excludeReservationId != null) {
                ps.setInt(4, excludeReservationId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error checking reservation conflict for point: " + pointId, e);
        }
    }

    public Reservation insert(Reservation res) {
        String sql = "INSERT INTO reservations (user_id, vehicle_id, point_id, start_time, end_time, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, res.getUserId());
            ps.setInt(2, res.getVehicleId());
            ps.setInt(3, res.getPointId());
            ps.setTimestamp(4, Timestamp.valueOf(res.getStartTime()));
            ps.setTimestamp(5, Timestamp.valueOf(res.getEndTime()));
            ps.setString(6, res.getStatus() != null ? res.getStatus() : "CONFIRMED");
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        res.setReservationId(keys.getInt(1));
                    }
                }
            }
            return res;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error inserting reservation", e);
        }
    }

    public Optional<Reservation> findById(int reservationId) {
        String sql = baseQuery() + "WHERE r.reservation_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding reservation ID: " + reservationId, e);
        }
        return Optional.empty();
    }

    public List<Reservation> findByUserId(int userId) {
        List<Reservation> list = new ArrayList<>();
        String sql = baseQuery() + "WHERE r.user_id = ? ORDER BY r.start_time DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching reservations for user: " + userId, e);
        }
        return list;
    }

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = baseQuery() + "ORDER BY r.start_time DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all reservations", e);
        }
        return list;
    }

    public boolean updateStatus(int reservationId, String newStatus) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newStatus);
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating status for reservation ID: " + reservationId, e);
        }
    }

    public Optional<Reservation> findUpcomingForPoint(int pointId) {
        String sql = baseQuery() + "WHERE r.point_id = ? AND r.status = 'CONFIRMED' AND r.start_time >= NOW() ORDER BY r.start_time ASC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, pointId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching upcoming reservation for point: " + pointId, e);
        }
        return Optional.empty();
    }

    private String baseQuery() {
        return "SELECT r.reservation_id, r.user_id, r.vehicle_id, r.point_id, r.start_time, r.end_time, r.status, r.created_at, " +
               "u.name AS user_name, u.email AS user_email, v.registration_number, v.model AS vehicle_model, " +
               "p.point_name, p.charger_power_kw, s.station_name, s.campus_location " +
               "FROM reservations r " +
               "JOIN users u ON r.user_id = u.user_id " +
               "JOIN vehicles v ON r.vehicle_id = v.vehicle_id " +
               "JOIN charging_points p ON r.point_id = p.point_id " +
               "JOIN charging_stations s ON p.station_id = s.station_id ";
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setUserId(rs.getInt("user_id"));
        r.setVehicleId(rs.getInt("vehicle_id"));
        r.setPointId(rs.getInt("point_id"));
        
        Timestamp st = rs.getTimestamp("start_time");
        if (st != null) r.setStartTime(st.toLocalDateTime());
        
        Timestamp et = rs.getTimestamp("end_time");
        if (et != null) r.setEndTime(et.toLocalDateTime());
        
        r.setStatus(rs.getString("status"));
        
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) r.setCreatedAt(ct.toLocalDateTime());

        r.setUserName(rs.getString("user_name"));
        r.setUserEmail(rs.getString("user_email"));
        r.setRegistrationNumber(rs.getString("registration_number"));
        r.setVehicleModel(rs.getString("vehicle_model"));
        r.setPointName(rs.getString("point_name"));
        r.setStationName(rs.getString("station_name"));
        r.setCampusLocation(rs.getString("campus_location"));
        r.setChargerPowerKw(rs.getDouble("charger_power_kw"));
        return r;
    }
}
