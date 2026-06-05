package com.sitmmio.dashboard.dto;

public record HistoricalSpeedRow(
        String routeId,
        String month,
        double averageSpeedKmh,
        long count
) {
}