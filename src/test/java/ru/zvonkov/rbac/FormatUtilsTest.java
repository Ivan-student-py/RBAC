package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {

    @Test
    void formatTable_WithHeadersAndRows_ReturnsFormattedTable() {
        String[] headers = {"Username", "Full Name", "Email"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"admin", "System Administrator", "admin@company.com"});
        rows.add(new String[]{"john", "John Smith", "john@company.com"});

        String result = FormatUtils.formatTable(headers, rows);

        assertTrue(result.contains("+"));
        assertTrue(result.contains("Username"));
        assertTrue(result.contains("Full Name"));
        assertTrue(result.contains("Email"));
        assertTrue(result.contains("admin"));
        assertTrue(result.contains("System Administrator"));
        assertTrue(result.contains("john"));
        assertTrue(result.contains("John Smith"));
    }

    @Test
    void formatTable_EmptyRows_ReturnsHeadersOnly() {
        String[] headers = {"Name", "Age"};
        List<String[]> rows = new ArrayList<>();

        String result = FormatUtils.formatTable(headers, rows);

        assertTrue(result.contains("Name"));
        assertTrue(result.contains("Age"));
        assertTrue(result.contains("+"));
    }

    @Test
    void formatBox_WrapsTextInBox() {
        String text = "Hello World";

        String result = FormatUtils.formatBox(text);

        assertTrue(result.startsWith("+"));
        assertTrue(result.contains("|"));
        assertTrue(result.contains("Hello World"));
        assertTrue(result.endsWith("+\n"));
    }

    @Test
    void formatBox_MultiLineText_WrapsAllLines() {
        String text = "Line 1\nLine 2\nLine 3";

        String result = FormatUtils.formatBox(text);

        assertTrue(result.contains("Line 1"));
        assertTrue(result.contains("Line 2"));
        assertTrue(result.contains("Line 3"));
        assertTrue(result.contains("|"));
        assertTrue(result.contains("+"));
    }

    @Test
    void formatHeader_ReturnsFormattedHeader() {
        String text = "Section Title";

        String result = FormatUtils.formatHeader(text);

        assertTrue(result.contains("Section Title"));
        assertTrue(result.contains("====="));
    }

    @Test
    void truncate_ShortText_ReturnsOriginal() {
        String text = "Short";

        String result = FormatUtils.truncate(text, 10);

        assertEquals("Short", result);
    }

    @Test
    void truncate_LongText_AddsEllipsis() {
        String text = "This is a very long text that needs to be truncated";

        String result = FormatUtils.truncate(text, 20);

        assertEquals("This is a very lo...", result);
        assertEquals(20, result.length());
    }

    @Test
    void padRight_ShortText_AddsSpaces() {
        String text = "Hello";

        String result = FormatUtils.padRight(text, 10);

        assertEquals("Hello     ", result);
        assertEquals(10, result.length());
    }

    @Test
    void padRight_LongText_ReturnsOriginal() {
        String text = "VeryLongText";

        String result = FormatUtils.padRight(text, 5);

        assertEquals("VeryLongText", result);
    }

    @Test
    void padLeft_ShortText_AddsSpaces() {
        String text = "World";

        String result = FormatUtils.padLeft(text, 10);

        assertEquals("     World", result);
        assertEquals(10, result.length());
    }

    @Test
    void padLeft_LongText_ReturnsOriginal() {
        String text = "VeryLongText";

        String result = FormatUtils.padLeft(text, 5);

        assertEquals("VeryLongText", result);
    }

    @Test
    void formatTable_NullHeaders_ReturnsEmpty() {
        String[] headers = null;
        List<String[]> rows = new ArrayList<>();

        String result = FormatUtils.formatTable(headers, rows);

        assertEquals("", result);
    }

    @Test
    void formatTable_EmptyHeaders_ReturnsEmpty() {
        String[] headers = {};
        List<String[]> rows = new ArrayList<>();

        String result = FormatUtils.formatTable(headers, rows);

        assertEquals("", result);
    }

    @Test
    void formatBox_NullText_ReturnsEmpty() {
        String result = FormatUtils.formatBox(null);

        assertEquals("", result);
    }

    @Test
    void formatBox_EmptyText_ReturnsEmpty() {
        String result = FormatUtils.formatBox("");

        assertEquals("", result);
    }

    @Test
    void truncate_NullText_ReturnsEmpty() {
        String result = FormatUtils.truncate(null, 10);

        assertEquals("", result);
    }

    @Test
    void padRight_NullText_ReturnsSpaces() {
        String result = FormatUtils.padRight(null, 5);

        assertEquals("     ", result);
        assertEquals(5, result.length());
    }

    @Test
    void padLeft_NullText_ReturnsSpaces() {
        String result = FormatUtils.padLeft(null, 5);

        assertEquals("     ", result);
        assertEquals(5, result.length());
    }
}