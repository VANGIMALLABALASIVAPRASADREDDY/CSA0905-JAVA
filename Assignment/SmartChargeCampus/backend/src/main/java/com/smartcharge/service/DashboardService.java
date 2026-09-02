package com.smartcharge.service;

import com.smartcharge.dao.CampusLoadDao;
import com.smartcharge.dao.ReportDao;
import com.smartcharge.dto.DashboardMetricsDto;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final ReportDao reportDao;
    private final CampusLoadDao campusLoadDao;

    public DashboardService(ReportDao reportDao, CampusLoadDao campusLoadDao) {
        this.reportDao = reportDao;
        this.campusLoadDao = campusLoadDao;
    }

    public DashboardMetricsDto getDashboardMetrics() {
        double currentLoad = campusLoadDao.getCurrentActiveLoadKw();
        double maxLoad = campusLoadDao.getMaxCampusLoadKw();
        return reportDao.getDashboardMetrics(currentLoad, maxLoad);
    }
}
