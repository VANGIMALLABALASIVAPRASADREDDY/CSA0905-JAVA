package com.smartcharge.service;

import com.smartcharge.dao.CampusLoadDao;
import com.smartcharge.exception.CampusLoadExceededException;
import org.springframework.stereotype.Service;

@Service
public class LoadManagementService {

    private final CampusLoadDao campusLoadDao;

    public LoadManagementService(CampusLoadDao campusLoadDao) {
        this.campusLoadDao = campusLoadDao;
    }

    public double getCurrentActiveLoad() {
        return campusLoadDao.getCurrentActiveLoadKw();
    }

    public double getMaxCampusCapacity() {
        return campusLoadDao.getMaxCampusLoadKw();
    }

    /**
     * Checks if allocating additional power will exceed the campus maximum load limit
     */
    public boolean canAccommodateLoad(double additionalPowerKw) {
        double current = getCurrentActiveLoad();
        double max = getMaxCampusCapacity();
        return (current + additionalPowerKw) <= max;
    }

    /**
     * Validates and throws CampusLoadExceededException if load limit would be breached
     */
    public void validateLoadAllocation(double additionalPowerKw) {
        double current = getCurrentActiveLoad();
        double max = getMaxCampusCapacity();
        double projected = current + additionalPowerKw;
        if (projected > max) {
            throw new CampusLoadExceededException(
                String.format("Campus EV load limit exceeded! Current load: %.1f kW, Requested: %.1f kW, Projected: %.1f kW, Max limit: %.1f kW",
                        current, additionalPowerKw, projected, max)
            );
        }
    }
}
