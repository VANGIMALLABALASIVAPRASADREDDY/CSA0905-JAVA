package com.smartcharge.dao;

import com.smartcharge.exception.DatabaseOperationException;
import com.smartcharge.model.ChargingStation;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class StationDao {

    private final DataSource dataSource;

    public StationDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * ============================================================
     * CSA0905 ACADEMIC REQUIREMENT: java.sql.Statement Demonstration
     * Meaningful fixed reference query without user-controlled input
     * ============================================================
     */
    public List<ChargingStation> findAllUsingStatement() {
        List<ChargingStation> stations = new ArrayList<>();
        String sql = "SELECT station_id, station_name, campus_location, maximum_load_kw, status FROM charging_stations ORDER BY station_id";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stations.add(mapStation(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error executing Statement for all charging stations", e);
        }
        return stations;
    }

    public List<ChargingStation> findAll() {
        return findAllUsingStatement();
    }

    public Optional<ChargingStation> findById(int stationId) {
        String sql = "SELECT station_id, station_name, campus_location, maximum_load_kw, status FROM charging_stations WHERE station_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapStation(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching station by ID: " + stationId, e);
        }
        return Optional.empty();
    }

    public ChargingStation insert(ChargingStation station) {
        String sql = "INSERT INTO charging_stations (station_name, campus_location, maximum_load_kw, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, station.getStationName());
            ps.setString(2, station.getCampusLocation());
            ps.setDouble(3, station.getMaximumLoadKw());
            ps.setString(4, station.getStatus());
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        station.setStationId(keys.getInt(1));
                    }
                }
            }
            return station;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error inserting charging station", e);
        }
    }

    public boolean update(ChargingStation station) {
        String sql = "UPDATE charging_stations SET station_name = ?, campus_location = ?, maximum_load_kw = ?, status = ? WHERE station_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, station.getStationName());
            ps.setString(2, station.getCampusLocation());
            ps.setDouble(3, station.getMaximumLoadKw());
            ps.setString(4, station.getStatus());
            ps.setInt(5, station.getStationId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error updating charging station ID: " + station.getStationId(), e);
        }
    }

    public boolean deleteById(int stationId) {
        String sql = "DELETE FROM charging_stations WHERE station_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, stationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error deleting charging station ID: " + stationId, e);
        }
    }

    private ChargingStation mapStation(ResultSet rs) throws SQLException {
        ChargingStation s = new ChargingStation();
        s.setStationId(rs.getInt("station_id"));
        s.setStationName(rs.getString("station_name"));
        s.setCampusLocation(rs.getString("campus_location"));
        s.setMaximumLoadKw(rs.getDouble("maximum_load_kw"));
        s.setStatus(rs.getString("status"));
        return s;
    }
}
