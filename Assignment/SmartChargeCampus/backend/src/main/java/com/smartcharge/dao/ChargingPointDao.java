package com.smartcharge.dao;

import com.smartcharge.exception.DatabaseOperationException;
import com.smartcharge.model.ChargingPoint;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ChargingPointDao {

    private final DataSource dataSource;

    public ChargingPointDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ChargingPoint> findAll() {
        List<ChargingPoint> list = new ArrayList<>();
        String sql = "SELECT p.point_id, p.station_id, p.point_name, p.charger_power_kw, p.connector_type, p.status, " +
                     "s.station_name, s.campus_location " +
                     "FROM charging_points p " +
                     "JOIN charging_stations s ON p.station_id = s.station_id " +
                     "ORDER BY s.station_id, p.point_id";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapChargingPoint(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all charging points", e);
        }
        return list;
    }

    public List<ChargingPoint> findByStationId(int stationId) {
        List<ChargingPoint> list = new ArrayList<>();
        String sql = "SELECT p.point_id, p.station_id, p.point_name, p.charger_power_kw, p.connector_type, p.status, " +
                     "s.station_name, s.campus_location " +
                     "FROM charging_points p " +
                     "JOIN charging_stations s ON p.station_id = s.station_id " +
                     "WHERE p.station_id = ? " +
                     "ORDER BY p.point_id";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapChargingPoint(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching charging points for station: " + stationId, e);
        }
        return list;
    }

    public Optional<ChargingPoint> findById(int pointId) {
        String sql = "SELECT p.point_id, p.station_id, p.point_name, p.charger_power_kw, p.connector_type, p.status, " +
                     "s.station_name, s.campus_location " +
                     "FROM charging_points p " +
                     "JOIN charging_stations s ON p.station_id = s.station_id " +
                     "WHERE p.point_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, pointId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapChargingPoint(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching charging point ID: " + pointId, e);
        }
        return Optional.empty();
    }

    public List<ChargingPoint> findCompatiblePoints(String connectorType) {
        List<ChargingPoint> list = new ArrayList<>();
        String sql = "SELECT p.point_id, p.station_id, p.point_name, p.charger_power_kw, p.connector_type, p.status, " +
                     "s.station_name, s.campus_location " +
                     "FROM charging_points p " +
                     "JOIN charging_stations s ON p.station_id = s.station_id " +
                     "WHERE p.connector_type = ? AND s.status = 'ACTIVE' " +
                     "ORDER BY p.charger_power_kw DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, connectorType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapChargingPoint(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching compatible charging points for connector: " + connectorType, e);
        }
        return list;
    }

    public ChargingPoint insert(ChargingPoint point) {
        String sql = "INSERT INTO charging_points (station_id, point_name, charger_power_kw, connector_type, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, point.getStationId());
            ps.setString(2, point.getPointName());
            ps.setDouble(3, point.getChargerPowerKw());
            ps.setString(4, point.getConnectorType());
            ps.setString(5, point.getStatus());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        point.setPointId(keys.getInt(1));
                    }
                }
            }
            return point;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error inserting charging point", e);
        }
    }

    public boolean update(ChargingPoint point) {
        String sql = "UPDATE charging_points SET station_id = ?, point_name = ?, charger_power_kw = ?, connector_type = ?, status = ? WHERE point_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, point.getStationId());
            ps.setString(2, point.getPointName());
            ps.setDouble(3, point.getChargerPowerKw());
            ps.setString(4, point.getConnectorType());
            ps.setString(5, point.getStatus());
            ps.setInt(6, point.getPointId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating charging point ID: " + point.getPointId(), e);
        }
    }

    public boolean updateStatus(int pointId, String newStatus) {
        String sql = "UPDATE charging_points SET status = ? WHERE point_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, newStatus);
            ps.setInt(2, pointId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating status for charging point ID: " + pointId, e);
        }
    }

    public boolean deleteById(int pointId) {
        String sql = "DELETE FROM charging_points WHERE point_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, pointId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting charging point ID: " + pointId, e);
        }
    }

    private ChargingPoint mapChargingPoint(ResultSet rs) throws SQLException {
        ChargingPoint p = new ChargingPoint();
        p.setPointId(rs.getInt("point_id"));
        p.setStationId(rs.getInt("station_id"));
        p.setPointName(rs.getString("point_name"));
        p.setChargerPowerKw(rs.getDouble("charger_power_kw"));
        p.setConnectorType(rs.getString("connector_type"));
        p.setStatus(rs.getString("status"));
        p.setStationName(rs.getString("station_name"));
        p.setCampusLocation(rs.getString("campus_location"));
        return p;
    }
}
