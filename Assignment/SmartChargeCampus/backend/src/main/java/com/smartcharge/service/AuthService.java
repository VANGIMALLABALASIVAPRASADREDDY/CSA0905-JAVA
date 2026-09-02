package com.smartcharge.service;

import com.smartcharge.dao.UserDao;
import com.smartcharge.dto.LoginRequest;
import com.smartcharge.dto.LoginResponse;
import com.smartcharge.model.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public LoginResponse authenticate(LoginRequest req) {
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        Optional<User> userOpt = userDao.findByEmailAndPassword(req.getEmail().trim(), req.getPassword().trim());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User u = userOpt.get();
        return new LoginResponse(u.getUserId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole());
    }

    public User getUserById(int userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + userId));
    }
}
