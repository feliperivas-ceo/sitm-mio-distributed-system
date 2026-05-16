package com.sitmmio.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private DateUtils() {}

    public static String format(LocalDateTime dt) {
        return dt == null ? "" : dt.format(FORMATTER);
    }

    public static LocalDateTime parse(String raw) {
        if (raw == null || raw.isBlank()) return LocalDateTime.now();
        try { return LocalDateTime.parse(raw, FORMATTER); }
        catch (Exception e) { return LocalDateTime.now(); }
    }

    public static String nowString() {
        return format(LocalDateTime.now());
    }
}
