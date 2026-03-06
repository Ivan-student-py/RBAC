package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void getCurrentDate_ReturnsValidDateFormat() {
        String date = DateUtils.getCurrentDate();

        assertTrue(date.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void getCurrentDateTime_ReturnsValidDateTimeFormat() {
        String dateTime = DateUtils.getCurrentDateTime();

        assertTrue(dateTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void isBefore_Date1BeforeDate2_ReturnsTrue() {
        assertTrue(DateUtils.isBefore("2023-01-01", "2023-12-31"));
        assertTrue(DateUtils.isBefore("2020-05-15", "2020-05-16"));
    }

    @Test
    void isBefore_Date1AfterDate2_ReturnsFalse() {
        assertFalse(DateUtils.isBefore("2023-12-31", "2023-01-01"));
        assertFalse(DateUtils.isBefore("2020-05-16", "2020-05-15"));
    }

    @Test
    void isBefore_SameDate_ReturnsFalse() {
        assertFalse(DateUtils.isBefore("2023-01-01", "2023-01-01"));
    }

    @Test
    void isBefore_NullDate1_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.isBefore(null, "2023-01-01");
        });
    }

    @Test
    void isBefore_NullDate2_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.isBefore("2023-01-01", null);
        });
    }

    @Test
    void isAfter_Date1AfterDate2_ReturnsTrue() {
        assertTrue(DateUtils.isAfter("2023-12-31", "2023-01-01"));
        assertTrue(DateUtils.isAfter("2020-05-16", "2020-05-15"));
    }

    @Test
    void isAfter_Date1BeforeDate2_ReturnsFalse() {
        assertFalse(DateUtils.isAfter("2023-01-01", "2023-12-31"));
        assertFalse(DateUtils.isAfter("2020-05-15", "2020-05-16"));
    }

    @Test
    void isAfter_SameDate_ReturnsFalse() {
        assertFalse(DateUtils.isAfter("2023-01-01", "2023-01-01"));
    }

    @Test
    void isAfter_NullDate1_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.isAfter(null, "2023-01-01");
        });
    }

    @Test
    void isAfter_NullDate2_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.isAfter("2023-01-01", null);
        });
    }

    @Test
    void addDays_AddsCorrectNumberOfDays() {
        assertEquals("2023-01-02", DateUtils.addDays("2023-01-01", 1));
        assertEquals("2023-01-11", DateUtils.addDays("2023-01-01", 10));
        assertEquals("2023-02-01", DateUtils.addDays("2023-01-01", 31));
    }

    @Test
    void addDays_SubtractsDaysWhenNegative() {
        assertEquals("2022-12-31", DateUtils.addDays("2023-01-01", -1));
        assertEquals("2022-12-22", DateUtils.addDays("2023-01-01", -10));
    }

    @Test
    void addDays_NullDate_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.addDays(null, 5);
        });
    }

    @Test
    void formatRelativeTime_Today() {
        String today = DateUtils.getCurrentDate();
        assertEquals("today", DateUtils.formatRelativeTime(today));
    }

    @Test
    void formatRelativeTime_Tomorrow() {
        String tomorrow = DateUtils.addDays(DateUtils.getCurrentDate(), 1);
        assertEquals("tomorrow", DateUtils.formatRelativeTime(tomorrow));
    }

    @Test
    void formatRelativeTime_Yesterday() {
        String yesterday = DateUtils.addDays(DateUtils.getCurrentDate(), -1);
        assertEquals("yesterday", DateUtils.formatRelativeTime(yesterday));
    }

    @Test
    void formatRelativeTime_InFuture() {
        String future = DateUtils.addDays(DateUtils.getCurrentDate(), 5);
        assertEquals("in 5 days", DateUtils.formatRelativeTime(future));
    }

    @Test
    void formatRelativeTime_InPast() {
        String past = DateUtils.addDays(DateUtils.getCurrentDate(), -3);
        assertEquals("3 days ago", DateUtils.formatRelativeTime(past));
    }

    @Test
    void formatRelativeTime_NullDate_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.formatRelativeTime(null);
        });
    }

    @Test
    void parseDate_ValidDate_ReturnsLocalDate() {
        LocalDate date = DateUtils.parseDate("2023-05-15");

        assertEquals(2023, date.getYear());
        assertEquals(5, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    void parseDate_NullDate_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.parseDate(null);
        });
    }

    @Test
    void formatDate_LocalDate_ReturnsString() {
        LocalDate date = LocalDate.of(2023, 5, 15);
        String result = DateUtils.formatDate(date);

        assertEquals("2023-05-15", result);
    }

    @Test
    void formatDate_NullDate_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.formatDate(null);
        });
    }

    @Test
    void getCurrentDate_IsToday() {
        String currentDate = DateUtils.getCurrentDate();
        LocalDate parsed = LocalDate.parse(currentDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        assertEquals(LocalDate.now().getYear(), parsed.getYear());
        assertEquals(LocalDate.now().getMonthValue(), parsed.getMonthValue());
        assertEquals(LocalDate.now().getDayOfMonth(), parsed.getDayOfMonth());
    }
}