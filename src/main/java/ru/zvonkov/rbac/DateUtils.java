package ru.zvonkov.rbac;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public final class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATETIME_SHORT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateUtils() {}

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    public static String getCurrentDateTimeShort() {
        return LocalDateTime.now().format(DATETIME_SHORT_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        LocalDate d1 = LocalDate.parse(date1, DATE_FORMATTER);
        LocalDate d2 = LocalDate.parse(date2, DATE_FORMATTER);

        return d1.isBefore(d2);
    }

    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        LocalDate d1 = LocalDate.parse(date1, DATE_FORMATTER);
        LocalDate d2 = LocalDate.parse(date2, DATE_FORMATTER);

        return d1.isAfter(d2);
    }

    public static boolean isAfterDateTime(String dateTime1, String dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        LocalDateTime d1 = LocalDateTime.parse(dateTime1, DATETIME_SHORT_FORMATTER);
        LocalDateTime d2 = LocalDateTime.parse(dateTime2, DATETIME_SHORT_FORMATTER);

        return d1.isAfter(d2);
    }

    public static boolean isBeforeDateTime(String dateTime1, String dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            throw new IllegalArgumentException("Dates cannot be null");
        }

        LocalDateTime d1 = LocalDateTime.parse(dateTime1, DATETIME_SHORT_FORMATTER);
        LocalDateTime d2 = LocalDateTime.parse(dateTime2, DATETIME_SHORT_FORMATTER);

        return d1.isBefore(d2);
    }

    public static String addDays(String date, int days) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        LocalDate d = LocalDate.parse(date, DATE_FORMATTER);
        LocalDate newDate = d.plusDays(days);

        return newDate.format(DATE_FORMATTER);
    }

    public static String formatRelativeTime(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        LocalDate targetDate = LocalDate.parse(date, DATE_FORMATTER);
        LocalDate today = LocalDate.now();

        long daysBetween = ChronoUnit.DAYS.between(today, targetDate);

        if (daysBetween == 0) {
            return "today";
        } else if (daysBetween == 1) {
            return "tomorrow";
        } else if (daysBetween == -1) {
            return "yesterday";
        } else if (daysBetween > 0) {
            return "in " + daysBetween + " days";
        } else {
            return Math.abs(daysBetween) + " days ago";
        }
    }

    public static LocalDate parseDate(String date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return date.format(DATE_FORMATTER);
    }
}