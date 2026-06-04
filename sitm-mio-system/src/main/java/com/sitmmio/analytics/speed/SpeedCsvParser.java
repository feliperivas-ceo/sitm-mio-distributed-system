package com.sitmmio.analytics.speed;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class SpeedCsvParser {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Optional<SpeedRecord> parse(String line) {
        try {
            if (line == null || line.isBlank()) {
                return Optional.empty();
            }

            String[] c = line.split(",");

            if (c.length < 12) {
                return Optional.empty();
            }

            String busId = c[11].trim();
            String routeId = c[7].trim();
            double odometer = Double.parseDouble(c[4].trim());
            LocalDateTime timestamp = LocalDateTime.parse(c[10].trim(), TIMESTAMP_FORMAT);

            if (odometer < 0) {
                return Optional.empty();
            }

            return Optional.of(new SpeedRecord(busId, routeId, odometer, timestamp));

        } catch (Exception e) {
            return Optional.empty();
        }
    }
}