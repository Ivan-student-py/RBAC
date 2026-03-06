package ru.zvonkov.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static AssignmentMetadata now(String assignedBy, String reason) {
        ValidationUtils.requireNonEmpty(assignedBy, "Assigned by");
        String currentTime = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy.trim(), currentTime, reason);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Assigned by: ").append(assignedBy).append("\n");
        sb.append("Assigned at: ").append(assignedAt).append("\n");
        if (reason != null && !reason.trim().isEmpty()) {
            sb.append("Reason: ").append(reason.trim()).append("\n");
        } else {
            sb.append("Reason: (not specified)\n");
        }
        return sb.toString();
    }
}