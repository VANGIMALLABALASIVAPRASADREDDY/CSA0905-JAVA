package com.smartcharge.service;

import com.smartcharge.dao.VehicleDao;
import com.smartcharge.exception.InvalidVehicleException;
import com.smartcharge.model.Vehicle;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleDao vehicleDao;

    public VehicleService(VehicleDao vehicleDao) {
        this.vehicleDao = vehicleDao;
    }

    public List<Vehicle> getVehiclesByUserId(int userId) {
        return vehicleDao.findByUserId(userId);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleDao.findAll();
    }

    public Vehicle getVehicleById(int vehicleId) {
        return vehicleDao.findById(vehicleId)
                .orElseThrow(() -> new InvalidVehicleException("Vehicle not found for ID: " + vehicleId));
    }

    public Vehicle registerVehicle(Vehicle vehicle) {
        validateVehicle(vehicle, true);
        return vehicleDao.insert(vehicle);
    }

    public Vehicle updateVehicle(int vehicleId, Vehicle updatedData) {
        Vehicle existing = getVehicleById(vehicleId);
        updatedData.setVehicleId(vehicleId);
        updatedData.setUserId(existing.getUserId());

        if (!existing.getRegistrationNumber().equalsIgnoreCase(updatedData.getRegistrationNumber())) {
            validateVehicle(updatedData, true);
        } else {
            validateVehicle(updatedData, false);
        }

        boolean success = vehicleDao.update(updatedData);
        if (!success) {
            throw new InvalidVehicleException("Failed to update vehicle ID: " + vehicleId);
        }
        return getVehicleById(vehicleId);
    }

    public boolean deleteVehicle(int vehicleId) {
        getVehicleById(vehicleId); // verify exists
        return vehicleDao.deleteById(vehicleId);
    }

    private void validateVehicle(Vehicle vehicle, boolean checkUniqueReg) {
        if (vehicle.getRegistrationNumber() == null || vehicle.getRegistrationNumber().trim().isEmpty()) {
            throw new InvalidVehicleException("Vehicle registration number is required");
        }
        if (vehicle.getManufacturer() == null || vehicle.getManufacturer().trim().isEmpty()) {
            throw new InvalidVehicleException("Manufacturer is required");
        }
        if (vehicle.getModel() == null || vehicle.getModel().trim().isEmpty()) {
            throw new InvalidVehicleException("Model is required");
        }
        if (vehicle.getBatteryCapacityKwh() <= 0.0 || vehicle.getBatteryCapacityKwh() > 300.0) {
            throw new InvalidVehicleException("Battery capacity must be greater than 0 and realistic (up to 300 kWh)");
        }
        if (vehicle.getConnectorType() == null || vehicle.getConnectorType().trim().isEmpty()) {
            throw new InvalidVehicleException("Connector type is required (e.g., Type 2, CCS2, CHAdeMO, GB/T)");
        }
        if (checkUniqueReg && vehicleDao.existsByRegistrationNumber(vehicle.getRegistrationNumber())) {
            throw new InvalidVehicleException("A vehicle with registration number '" + vehicle.getRegistrationNumber() + "' is already registered");
        }
    }
}
