package com.sitmmio.analytics.speed;

import java.time.LocalDateTime;

public record SpeedRecord(
        String busId,
        String routeId,
        double odometer,
        LocalDateTime timestamp
) {
    public String monthKey() {
        return timestamp.getYear() + "-" + String.format("%02d", timestamp.getMonthValue());
    }
}