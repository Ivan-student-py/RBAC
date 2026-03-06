package ru.zvonkov.rbac;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<AuditEntry> entries;

    public AuditLog() {
        this.entries = new ArrayList<>();
    }

    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        entries.add(entry);
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(entry -> entry.performer().equals(performer))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(entry -> entry.action().equals(action))
                .collect(Collectors.toList());
    }

    public void printLog() {
        if (entries.isEmpty()) {
            System.out.println("Audit log is empty");
            return;
        }

        System.out.println("=== AUDIT LOG ===");
        System.out.println("Total entries: " + entries.size());
        System.out.println();

        for (int i = 0; i < entries.size(); i++) {
            AuditEntry entry = entries.get(i);
            System.out.println("[" + (i + 1) + "] " + entry.timestamp());
            System.out.println("    Action:    " + entry.action());
            System.out.println("    Performer: " + entry.performer());
            System.out.println("    Target:    " + entry.target());
            System.out.println("    Details:   " + entry.details());
            System.out.println();
        }
    }

    public void saveToFile(String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("=== AUDIT LOG ===\n");
            writer.write("Total entries: " + entries.size() + "\n\n");

            for (AuditEntry entry : entries) {
                writer.write("[" + entry.timestamp() + "] ");
                writer.write(entry.action() + " | ");
                writer.write(entry.performer() + " → " + entry.target());
                if (entry.details() != null && !entry.details().isEmpty()) {
                    writer.write(" | " + entry.details());
                }
                writer.write("\n");
            }
        }
    }
}