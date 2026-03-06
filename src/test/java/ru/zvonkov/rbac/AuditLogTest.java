package ru.zvonkov.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    void log_AddsEntryToLog() {
        auditLog.log("user-create", "admin", "alice", "Created new user");

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(1, entries.size());

        AuditEntry entry = entries.get(0);
        assertEquals("user-create", entry.action());
        assertEquals("admin", entry.performer());
        assertEquals("alice", entry.target());
        assertEquals("Created new user", entry.details());
    }

    @Test
    void getAll_ReturnsAllEntries() {
        auditLog.log("user-create", "admin", "alice", "Created user");
        auditLog.log("role-create", "admin", "Admin", "Created role");
        auditLog.log("assign-role", "admin", "alice → Admin", "Assigned role");

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(3, entries.size());
    }

    @Test
    void getByPerformer_FiltersByPerformer() {
        auditLog.log("user-create", "admin", "alice", "Created user");
        auditLog.log("role-create", "manager", "Viewer", "Created role");
        auditLog.log("assign-role", "admin", "bob → Manager", "Assigned role");

        List<AuditEntry> adminEntries = auditLog.getByPerformer("admin");
        assertEquals(2, adminEntries.size());
        assertTrue(adminEntries.stream().allMatch(e -> e.performer().equals("admin")));

        List<AuditEntry> managerEntries = auditLog.getByPerformer("manager");
        assertEquals(1, managerEntries.size());
        assertEquals("manager", managerEntries.get(0).performer());
    }

    @Test
    void getByAction_FiltersByAction() {
        auditLog.log("user-create", "admin", "alice", "Created user");
        auditLog.log("user-delete", "admin", "bob", "Deleted user");
        auditLog.log("user-create", "admin", "charlie", "Created user");

        List<AuditEntry> createEntries = auditLog.getByAction("user-create");
        assertEquals(2, createEntries.size());
        assertTrue(createEntries.stream().allMatch(e -> e.action().equals("user-create")));

        List<AuditEntry> deleteEntries = auditLog.getByAction("user-delete");
        assertEquals(1, deleteEntries.size());
        assertEquals("user-delete", deleteEntries.get(0).action());
    }

    @Test
    void saveToFile_WritesLogToFile() throws IOException {
        auditLog.log("user-create", "admin", "alice", "Created user");
        auditLog.log("role-create", "admin", "Admin", "Created role");

        String filename = "test_audit_log.txt";
        auditLog.saveToFile(filename);

        assertTrue(Files.exists(java.nio.file.Paths.get(filename)));

        String content = Files.readString(java.nio.file.Paths.get(filename));
        assertTrue(content.contains("=== AUDIT LOG ==="));
        assertTrue(content.contains("Total entries: 2"));
        assertTrue(content.contains("user-create"));
        assertTrue(content.contains("role-create"));
        assertTrue(content.contains("admin"));

        Files.delete(java.nio.file.Paths.get(filename));
    }

    @Test
    void saveToFile_EmptyLog_WritesEmptyMessage() throws IOException {
        String filename = "test_audit_log_empty.txt";
        auditLog.saveToFile(filename);

        String content = Files.readString(java.nio.file.Paths.get(filename));
        assertTrue(content.contains("=== AUDIT LOG ==="));
        assertTrue(content.contains("Total entries: 0"));

        Files.delete(java.nio.file.Paths.get(filename));
    }

    @Test
    void printLog_PrintsToConsole() {
        auditLog.log("user-create", "admin", "alice", "Created user");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        auditLog.printLog();

        String output = outContent.toString();
        assertTrue(output.contains("=== AUDIT LOG ==="));
        assertTrue(output.contains("Total entries: 1"));
        assertTrue(output.contains("user-create"));
        assertTrue(output.contains("admin"));
        assertTrue(output.contains("alice"));

        System.setOut(System.out);
    }

    @Test
    void printLog_EmptyLog_PrintsEmptyMessage() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        auditLog.printLog();

        String output = outContent.toString();
        assertTrue(output.contains("Audit log is empty"));

        System.setOut(System.out);
    }
}
