package ru.zvonkov.rbac;

import java.util.List;

public final class FormatUtils {

    private FormatUtils() {}

    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "";
        }

        int[] columnWidths = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = Math.max(columnWidths[i], headers[i].length());
        }

        for (String[] row : rows) {
            if (row == null) continue;
            for (int i = 0; i < Math.min(row.length, headers.length); i++) {
                columnWidths[i] = Math.max(columnWidths[i], row[i].length());
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("+");
        for (int width : columnWidths) {
            sb.append("-".repeat(width + 2)).append("+");
        }
        sb.append("\n");

        sb.append("|");
        for (int i = 0; i < headers.length; i++) {
            sb.append(" ").append(padRight(headers[i], columnWidths[i])).append(" |");
        }
        sb.append("\n");

        sb.append("+");
        for (int width : columnWidths) {
            sb.append("=".repeat(width + 2)).append("+");
        }
        sb.append("\n");

        for (String[] row : rows) {
            sb.append("|");
            for (int i = 0; i < headers.length; i++) {
                String cell = (row != null && i < row.length) ? row[i] : "";
                sb.append(" ").append(padRight(cell, columnWidths[i])).append(" |");
            }
            sb.append("\n");
        }

        sb.append("+");
        for (int width : columnWidths) {
            sb.append("-".repeat(width + 2)).append("+");
        }
        sb.append("\n");

        return sb.toString();
    }

    public static String formatBox(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String[] lines = text.split("\n");
        int maxWidth = 0;

        for (String line : lines) {
            maxWidth = Math.max(maxWidth, line.length());
        }

        StringBuilder sb = new StringBuilder();

        sb.append("+").append("-".repeat(maxWidth + 2)).append("+\n");

        for (String line : lines) {
            sb.append("| ").append(padRight(line, maxWidth)).append(" |\n");
        }

        sb.append("+").append("-".repeat(maxWidth + 2)).append("+\n");

        return sb.toString();
    }

    public static String formatHeader(String text) {
        if (text == null) return "";

        String line = "=".repeat(50);
        return "\n" + line + "\n" + text + "\n" + line + "\n";
    }

    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;

        return text.substring(0, maxLength - 3) + "...";
    }

    public static String padRight(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;

        return text + " ".repeat(length - text.length());
    }

    public static String padLeft(String text, int length) {
        if (text == null) text = "";
        if (text.length() >= length) return text;

        return " ".repeat(length - text.length()) + text;
    }
}