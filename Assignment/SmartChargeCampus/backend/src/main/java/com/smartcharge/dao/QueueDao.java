package com.smartcharge.dao;

import com.smartcharge.exception.DatabaseOperationException;
import com.smartcharge.model.QueueEntry;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class QueueDao {

    private final DataSource dataSource;

    public QueueDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public QueueEntry insert(QueueEntry qe) {
        String sql = "INSERT INTO queue_entries (user_id, vehicle_id, preferred_location, current_battery_percent, " +
                     "target_battery_percent, requested_time, departure_time, priority_score, queue_position, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, qe.getUserId());
            ps.setInt(2, qe.getVehicleId());
            ps.setString(3, qe.getPreferredLocation());
            ps.setDouble(4, qe.getCurrentBatteryPercent());
            ps.setDouble(5, qe.getTargetBatteryPercent());
            ps.setTimestamp(6, Timestamp.valueOf(qe.getRequestedTime()));
            ps.setTimestamp(7, Timestamp.valueOf(qe.getDepartureTime()));
            ps.setDouble(8, qe.getPriorityScore());
            ps.setInt(9, qe.getQueuePosition());
            ps.setString(10, qe.getStatus() != null ? qe.getStatus() : "WAITING");
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        qe.setQueueId(keys.getInt(1));
                    }
                }
            }
            return qe;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error adding vehicle to virtual queue", e);
        }
    }

    public List<QueueEntry> findActiveQueue() {
        List<QueueEntry> list = new ArrayList<>();
        String sql = baseQuery() + "WHERE q.status = 'WAITING' ORDER BY q.priority_score DESC, q.created_at ASC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            int pos = 1;
            while (rs.next()) {
                QueueEntry qe = mapQueue(rs);
                qe.setQueuePosition(pos++);
                list.add(qe);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching virtual queue entries", e);
        }
        return list;
    }

    public List<QueueEntry> findAll() {
        List<QueueEntry> list = new ArrayList<>();
        String sql = baseQuery() + "ORDER BY q.queue_id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapQueue(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all queue entries", e);
        }
        return list;
    }

    public Optional<QueueEntry> findById(int queueId) {
        String sql = baseQuery() + "WHERE q.queue_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, queueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapQueue(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching queue entry by ID: " + queueId, e);
        }
        return Optional.empty();
    }

    public boolean updateStatus(int queueId, String newStatus) {
        String sql = "UPDATE queue_entries SET status = ? WHERE queue_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newStatus);
            ps.setInt(2, queueId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating queue entry status for ID: " + queueId, e);
        }
    }

    public int getActiveQueueCount() {
        String sql = "SELECT COUNT(*) FROM queue_entries WHERE status = 'WAITING'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error counting active queue entries", e);
        }
        return 0;
    }

    private String baseQuery() {
        return "SELECT q.queue_id, q.user_id, q.vehicle_id, q.preferred_location, q.current_battery_percent, " +
               "q.target_battery_percent, q.requested_time, q.departure_time, q.priority_score, q.queue_position, " +
               "q.status, q.created_at, u.name AS user_name, u.email AS user_email, " +
               "v.registration_number, v.model AS vehicle_model, v.connector_type, v.battery_capacity_kwh " +
               "FROM queue_entries q " +
               "JOIN users u ON q.user_id = u.user_id " +
               "JOIN vehicles v ON q.vehicle_id = v.vehicle_id ";
    }

    private QueueEntry mapQueue(ResultSet rs) throws SQLException {
        QueueEntry qe = new QueueEntry();
        qe.setQueueId(rs.getInt("queue_id"));
        qe.setUserId(rs.getInt("user_id"));
        qe.setVehicleId(rs.getInt("vehicle_id"));
        qe.setPreferredLocation(rs.getString("preferred_location"));
        qe.setCurrentBatteryPercent(rs.getDouble("current_battery_percent"));
        qe.setTargetBatteryPercent(rs.getDouble("target_battery_percent"));
        
        Timestamp rt = rs.getTimestamp("requested_time");
        if (rt != null) qe.setRequestedTime(rt.toLocalDateTime());
        
        Timestamp dt = rs.getTimestamp("departure_time");
        if (dt != null) qe.setDepartureTime(dt.toLocalDateTime());

        qe.setPriorityScore(rs.getDouble("priority_score"));
        qe.setQueuePosition(rs.getInt("queue_position"));
        qe.setStatus(rs.getString("status"));
        
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) qe.setCreatedAt(ct.toLocalDateTime());

        qe.setUserName(rs.getString("user_name"));
        qe.setUserEmail(rs.getString("user_email"));
        qe.setRegistrationNumber(rs.getString("registration_number"));
        qe.setVehicleModel(rs.getString("vehicle_model"));
        qe.setConnectorType(rs.getString("connector_type"));
        qe.setBatteryCapacityKwh(rs.getDouble("battery_capacity_kwh"));
        return qe;
    }
}
