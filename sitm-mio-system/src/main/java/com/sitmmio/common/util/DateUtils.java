package com.sitmmio.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private DateUtils() {}

    public static String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "N/A";
        return dt.format(DISPLAY_FORMATTER);
    }

    public static String format(LocalDateTime dt) {
        return dt == null ? "" : dt.format(ISO_FORMATTER);
    }

    public static LocalDateTime parse(String raw) {
        if (raw == null || raw.isBlank()) return LocalDateTime.now();
        try { return LocalDateTime.parse(raw, ISO_FORMATTER); }
        catch (Exception e) { return LocalDateTime.now(); }
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static String nowString() {
        return format(LocalDateTime.now());
    }
}
