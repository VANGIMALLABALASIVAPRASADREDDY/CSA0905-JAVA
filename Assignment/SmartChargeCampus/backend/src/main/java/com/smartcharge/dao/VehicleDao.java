package com.smartcharge.dao;

import com.smartcharge.exception.DatabaseOperationException;
import com.smartcharge.model.Vehicle;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class VehicleDao {

    private final DataSource dataSource;

    public VehicleDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * SELECT vehicle by ID using PreparedStatement
     */
    public Optional<Vehicle> findById(int vehicleId) {
        String sql = "SELECT v.vehicle_id, v.user_id, v.registration_number, v.manufacturer, v.model, " +
                     "v.battery_capacity_kwh, v.connector_type, v.created_at, u.name AS owner_name " +
                     "FROM vehicles v " +
                     "JOIN users u ON v.user_id = u.user_id " +
                     "WHERE v.vehicle_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, vehicleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapVehicle(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching vehicle by ID: " + vehicleId, e);
        }
        return Optional.empty();
    }

    /**
     * SELECT vehicles by user_id using PreparedStatement
     */
    public List<Vehicle> findByUserId(int userId) {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.vehicle_id, v.user_id, v.registration_number, v.manufacturer, v.model, " +
                     "v.battery_capacity_kwh, v.connector_type, v.created_at, u.name AS owner_name " +
                     "FROM vehicles v " +
                     "JOIN users u ON v.user_id = u.user_id " +
                     "WHERE v.user_id = ? " +
                     "ORDER BY v.vehicle_id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapVehicle(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching vehicles for user: " + userId, e);
        }
        return list;
    }

    /**
     * SELECT all vehicles using PreparedStatement
     */
    public List<Vehicle> findAll() {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT v.vehicle_id, v.user_id, v.registration_number, v.manufacturer, v.model, " +
                     "v.battery_capacity_kwh, v.connector_type, v.created_at, u.name AS owner_name " +
                     "FROM vehicles v " +
                     "JOIN users u ON v.user_id = u.user_id " +
                     "ORDER BY v.vehicle_id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapVehicle(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error listing all vehicles", e);
        }
        return list;
    }

    /**
     * INSERT vehicle using PreparedStatement with ? placeholders
     */
    public Vehicle insert(Vehicle vehicle) {
        String sql = "INSERT INTO vehicles (user_id, registration_number, manufacturer, model, battery_capacity_kwh, connector_type) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, vehicle.getUserId());
            ps.setString(2, vehicle.getRegistrationNumber().trim().toUpperCase());
            ps.setString(3, vehicle.getManufacturer().trim());
            ps.setString(4, vehicle.getModel().trim());
            ps.setDouble(5, vehicle.getBatteryCapacityKwh());
            ps.setString(6, vehicle.getConnectorType());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        vehicle.setVehicleId(keys.getInt(1));
                    }
                }
            }
            return vehicle;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error inserting vehicle: " + vehicle.getRegistrationNumber(), e);
        }
    }

    /**
     * UPDATE vehicle using PreparedStatement
     */
    public boolean update(Vehicle vehicle) {
        String sql = "UPDATE vehicles SET manufacturer = ?, model = ?, battery_capacity_kwh = ?, connector_type = ?, registration_number = ? " +
                     "WHERE vehicle_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, vehicle.getManufacturer().trim());
            ps.setString(2, vehicle.getModel().trim());
            ps.setDouble(3, vehicle.getBatteryCapacityKwh());
            ps.setString(4, vehicle.getConnectorType());
            ps.setString(5, vehicle.getRegistrationNumber().trim().toUpperCase());
            ps.setInt(6, vehicle.getVehicleId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating vehicle ID: " + vehicle.getVehicleId(), e);
        }
    }

    /**
     * DELETE vehicle using PreparedStatement
     */
    public boolean deleteById(int vehicleId) {
        String sql = "DELETE FROM vehicles WHERE vehicle_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, vehicleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting vehicle ID: " + vehicleId, e);
        }
    }

    public boolean existsByRegistrationNumber(String regNumber) {
        String sql = "SELECT 1 FROM vehicles WHERE registration_number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, regNumber.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error checking registration number uniqueness", e);
        }
    }

    private Vehicle mapVehicle(ResultSet rs) throws SQLException {
        Vehicle v = new Vehicle();
        v.setVehicleId(rs.getInt("vehicle_id"));
        v.setUserId(rs.getInt("user_id"));
        v.setRegistrationNumber(rs.getString("registration_number"));
        v.setManufacturer(rs.getString("manufacturer"));
        v.setModel(rs.getString("model"));
        v.setBatteryCapacityKwh(rs.getDouble("battery_capacity_kwh"));
        v.setConnectorType(rs.getString("connector_type"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            v.setCreatedAt(ts.toLocalDateTime());
        }
        v.setOwnerName(rs.getString("owner_name"));
        return v;
    }
}
