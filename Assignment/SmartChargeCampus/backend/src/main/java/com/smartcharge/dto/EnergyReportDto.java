package com.smartcharge.dto;

import java.util.Map;

public class EnergyReportDto {
    private double todayEnergyKwh;
    private double weekEnergyKwh;
    private double monthEnergyKwh;
    private double totalEnergyKwh;
    private double todayRevenue;
    private double weekRevenue;
    private double monthRevenue;
    private double totalRevenue;
    private Map<String, Double> stationEnergyDistribution;

    public EnergyReportDto() {}

    public double getTodayEnergyKwh() {
        return todayEnergyKwh;
    }

    public void setTodayEnergyKwh(double todayEnergyKwh) {
        this.todayEnergyKwh = todayEnergyKwh;
    }

    public double getWeekEnergyKwh() {
        return weekEnergyKwh;
    }

    public void setWeekEnergyKwh(double weekEnergyKwh) {
        this.weekEnergyKwh = weekEnergyKwh;
    }

    public double getMonthEnergyKwh() {
        return monthEnergyKwh;
    }

    public void setMonthEnergyKwh(double monthEnergyKwh) {
        this.monthEnergyKwh = monthEnergyKwh;
    }

    public double getTotalEnergyKwh() {
        return totalEnergyKwh;
    }

    public void setTotalEnergyKwh(double totalEnergyKwh) {
        this.totalEnergyKwh = totalEnergyKwh;
    }

    public double getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(double todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public double getWeekRevenue() {
        return weekRevenue;
    }

    public void setWeekRevenue(double weekRevenue) {
        this.weekRevenue = weekRevenue;
    }

    public double getMonthRevenue() {
        return monthRevenue;
    }

    public void setMonthRevenue(double monthRevenue) {
        this.monthRevenue = monthRevenue;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Map<String, Double> getStationEnergyDistribution() {
        return stationEnergyDistribution;
    }

    public void setStationEnergyDistribution(Map<String, Double> stationEnergyDistribution) {
        this.stationEnergyDistribution = stationEnergyDistribution;
    }
}
