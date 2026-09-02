package com.smartcharge.dao;

import com.smartcharge.exception.DatabaseOperationException;
import com.smartcharge.model.Payment;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentDao {

    private final DataSource dataSource;

    public PaymentDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Payment insert(Payment payment) {
        String sql = "INSERT INTO payments (session_id, amount, payment_method, payment_status) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setInt(1, payment.getSessionId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "PAID");
            
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        payment.setPaymentId(keys.getInt(1));
                    }
                }
            }
            return payment;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error recording payment for session: " + payment.getSessionId(), e);
        }
    }

    public List<Payment> findAll() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT pm.payment_id, pm.session_id, pm.amount, pm.payment_method, pm.payment_status, pm.payment_time, " +
                     "v.registration_number, u.name AS user_name, p.point_name, cs.energy_consumed_kwh " +
                     "FROM payments pm " +
                     "JOIN charging_sessions cs ON pm.session_id = cs.session_id " +
                     "JOIN vehicles v ON cs.vehicle_id = v.vehicle_id " +
                     "JOIN users u ON v.user_id = u.user_id " +
                     "JOIN charging_points p ON cs.point_id = p.point_id " +
                     "ORDER BY pm.payment_id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapPayment(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching all payments", e);
        }
        return list;
    }

    public List<Payment> findByUserId(int userId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT pm.payment_id, pm.session_id, pm.amount, pm.payment_method, pm.payment_status, pm.payment_time, " +
                     "v.registration_number, u.name AS user_name, p.point_name, cs.energy_consumed_kwh " +
                     "FROM payments pm " +
                     "JOIN charging_sessions cs ON pm.session_id = cs.session_id " +
                     "JOIN vehicles v ON cs.vehicle_id = v.vehicle_id " +
                     "JOIN users u ON v.user_id = u.user_id " +
                     "JOIN charging_points p ON cs.point_id = p.point_id " +
                     "WHERE u.user_id = ? " +
                     "ORDER BY pm.payment_id DESC";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPayment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error fetching payments for user: " + userId, e);
        }
        return list;
    }

    public Optional<Payment> findBySessionId(int sessionId) {
        String sql = "SELECT pm.payment_id, pm.session_id, pm.amount, pm.payment_method, pm.payment_status, pm.payment_time, " +
                     "v.registration_number, u.name AS user_name, p.point_name, cs.energy_consumed_kwh " +
                     "FROM payments pm " +
                     "JOIN charging_sessions cs ON pm.session_id = cs.session_id " +
                     "JOIN vehicles v ON cs.vehicle_id = v.vehicle_id " +
                     "JOIN users u ON v.user_id = u.user_id " +
                     "JOIN charging_points p ON cs.point_id = p.point_id " +
                     "WHERE pm.session_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPayment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Error finding payment for session: " + sessionId, e);
        }
        return Optional.empty();
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setSessionId(rs.getInt("session_id"));
        p.setAmount(rs.getDouble("amount"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setPaymentStatus(rs.getString("payment_status"));
        Timestamp pt = rs.getTimestamp("payment_time");
        if (pt != null) p.setPaymentTime(pt.toLocalDateTime());
        
        p.setRegistrationNumber(rs.getString("registration_number"));
        p.setUserName(rs.getString("user_name"));
        p.setPointName(rs.getString("point_name"));
        p.setEnergyConsumedKwh(rs.getDouble("energy_consumed_kwh"));
        return p;
    }
}
