package my_app.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

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
}

// Uso:
//long millis = DateUtils.localDateParaMillis(LocalDate.now());