package my_app.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    public static long localDateParaMillis(LocalDate data, ZoneId zoneId) {
        return data.atStartOfDay(zoneId)
                .toInstant()
                .toEpochMilli();
    }

    public static long localDateParaMillis(LocalDate data) {
        return localDateParaMillis(data, ZoneId.systemDefault());
    }

    public static LocalDate millisParaLocalDate(long millis, ZoneId zoneId) {
        return Instant.ofEpochMilli(millis)
                .atZone(zoneId)
                .toLocalDate();
    }

    public static LocalDate millisParaLocalDate(long millis) {
        return millisParaLocalDate(millis, ZoneId.systemDefault());
    }

    public static LocalDate millisParaLocalDateUTC(long millis) {
        return millisParaLocalDate(millis, ZoneOffset.UTC);
    }

    private static final DateTimeFormatter BR_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Converte um timestamp Long para String formatada em dd/MM/yyyy HH:mm
     */
    public static String millisToBrazilianDateTime(Long timestamp) {
        if (timestamp == null || timestamp == 0) return "";

        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(BR_FORMATTER);
    }

}

// Uso:
//long millis = DateUtils.localDateParaMillis(LocalDate.now());