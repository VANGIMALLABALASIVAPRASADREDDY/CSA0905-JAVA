package com.smartcharge.service;

import com.smartcharge.dao.CampusLoadDao;
import com.smartcharge.dao.ReportDao;
import com.smartcharge.dto.EnergyReportDto;
import com.smartcharge.dto.StationUtilizationDto;
import com.smartcharge.dto.SustainabilityReportDto;
import com.smartcharge.dto.UsageReportDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final ReportDao reportDao;
    private final CampusLoadDao campusLoadDao;

    public ReportService(ReportDao reportDao, CampusLoadDao campusLoadDao) {
        this.reportDao = reportDao;
        this.campusLoadDao = campusLoadDao;
    }

    /**
     * Executes MySQL Stored Procedure using CallableStatement
     */
    public List<StationUtilizationDto> getStationUtilization() {
        return reportDao.getStationUtilizationUsingCallableStatement();
    }

    public EnergyReportDto getEnergyReport() {
        return reportDao.getEnergyReport();
    }

    public UsageReportDto getUsageReport() {
        return reportDao.getUsageReport();
    }

    public SustainabilityReportDto getSustainabilityReport() {
        EnergyReportDto energyDto = reportDao.getEnergyReport();
        UsageReportDto usageDto = reportDao.getUsageReport();
        List<StationUtilizationDto> stations = reportDao.getStationUtilizationUsingCallableStatement();

        double avgUtil = stations.isEmpty() ? 0.0 :
                stations.stream().mapToDouble(StationUtilizationDto::getUtilizationPercent).average().orElse(0.0);

        SustainabilityReportDto dto = new SustainabilityReportDto();
        dto.setTotalEnergyDeliveredKwh(energyDto.getTotalEnergyKwh());
        dto.setTotalSessions(usageDto.getTotalSessions());
        dto.setAverageChargerUtilizationPercent(Math.round(avgUtil * 10.0) / 10.0);
        dto.setPeakCampusChargingLoadKw(campusLoadDao.getCurrentActiveLoadKw());
        dto.setMaxCampusCapacityKw(campusLoadDao.getMaxCampusLoadKw());
        dto.setAverageWaitTimeMinutes(usageDto.getAverageWaitingTimeMinutes());

        // Standard Central Electricity Authority (CEA) emission baseline:
        // Indian Grid Avg factor ~ 0.82 kg CO2 equivalent avoided per kWh displaced from gasoline vehicles
        double co2Saved = Math.round((energyDto.getTotalEnergyKwh() * 0.82) * 100.0) / 100.0;
        dto.setCo2SavedKg(co2Saved);
        dto.setCo2ConversionBasis("0.82 kg CO2 avoided per EV kWh delivered (Standard CEA India Grid vs ICE Baseline)");

        return dto;
    }
}
