package ru.zvonkov.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    private ReportGenerator reportGenerator;
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    @BeforeEach
    void setUp() {
        reportGenerator = new ReportGenerator();
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);

        User alice = User.validate("alice", "Alice A", "alice@example.com");
        User bob = User.validate("bob", "Bob B", "bob@example.com");
        userManager.add(alice);
        userManager.add(bob);

        Permission read = new Permission("read", "users", "Read users");
        Permission write = new Permission("write", "users", "Write users");

        Role viewer = new Role("Viewer", "Read-only");
        viewer.addPermission(read);
        roleManager.add(viewer);

        Role editor = new Role("Editor", "Edit access");
        editor.addPermission(read);
        editor.addPermission(write);
        roleManager.add(editor);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        assignmentManager.add(new PermanentAssignment(alice, viewer, meta));
        assignmentManager.add(new PermanentAssignment(bob, editor, meta));
    }

    @Test
    void generateUserReport_WithUsers_ReturnsFormattedReport() {
        String report = reportGenerator.generateUserReport(userManager, assignmentManager);

        assertTrue(report.contains("=== USER REPORT ==="));
        assertTrue(report.contains("Username"));
        assertTrue(report.contains("alice"));
        assertTrue(report.contains("bob"));
        assertTrue(report.contains("Total users: 2"));
        assertTrue(report.contains("Viewer"));
        assertTrue(report.contains("Editor"));
    }

    @Test
    void generateUserReport_EmptyManager_ReturnsEmptyMessage() {
        UserManager emptyManager = new UserManager();
        AssignmentManager emptyAssignments = new AssignmentManager(emptyManager, new RoleManager());

        String report = reportGenerator.generateUserReport(emptyManager, emptyAssignments);

        assertTrue(report.contains("=== USER REPORT ==="));
        assertTrue(report.contains("No users found"));
    }

    @Test
    void generateRoleReport_WithRoles_ReturnsFormattedReport() {
        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);

        assertTrue(report.contains("=== ROLE REPORT ==="));
        assertTrue(report.contains("Role Name"));
        assertTrue(report.contains("Viewer"));
        assertTrue(report.contains("Editor"));
        assertTrue(report.contains("Total roles: 2"));
        assertTrue(report.contains("Users"));
        assertTrue(report.contains("Permissions"));
    }

    @Test
    void generateRoleReport_EmptyManager_ReturnsEmptyMessage() {
        RoleManager emptyManager = new RoleManager();
        AssignmentManager emptyAssignments = new AssignmentManager(new UserManager(), emptyManager);

        String report = reportGenerator.generateRoleReport(emptyManager, emptyAssignments);

        assertTrue(report.contains("=== ROLE REPORT ==="));
        assertTrue(report.contains("No roles found"));
    }

    @Test
    void generatePermissionMatrix_WithPermissions_ReturnsFormattedMatrix() {
        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        assertTrue(report.contains("=== PERMISSION MATRIX ==="));
        assertTrue(report.contains("Username"));
        assertTrue(report.contains("users"));
        assertTrue(report.contains("alice"));
        assertTrue(report.contains("bob"));
        assertTrue(report.contains("✓"));
        assertTrue(report.contains("✗"));
        assertTrue(report.contains("Total users: 2"));
    }

    @Test
    void generatePermissionMatrix_EmptyManager_ReturnsEmptyMessage() {
        UserManager emptyManager = new UserManager();
        AssignmentManager emptyAssignments = new AssignmentManager(emptyManager, new RoleManager());

        String report = reportGenerator.generatePermissionMatrix(emptyManager, emptyAssignments);

        assertTrue(report.contains("=== PERMISSION MATRIX ==="));
        assertTrue(report.contains("No users found"));
    }

    @Test
    void generatePermissionMatrix_NoPermissions_ReturnsNoPermissionsMessage() {
        UserManager manager = new UserManager();
        RoleManager roleMgr = new RoleManager();
        AssignmentManager assignMgr = new AssignmentManager(manager, roleMgr);

        User user = User.validate("test", "Test User", "test@example.com");
        manager.add(user);

        String report = reportGenerator.generatePermissionMatrix(manager, assignMgr);

        assertTrue(report.contains("=== PERMISSION MATRIX ==="));
        assertTrue(report.contains("No permissions found"));
    }

    @Test
    void exportToFile_WritesReportToFile() throws IOException {
        String report = "=== TEST REPORT ===\nLine 1\nLine 2\nTotal: 2";
        String filename = "test_report.txt";

        reportGenerator.exportToFile(report, filename);

        assertTrue(Files.exists(Paths.get(filename)));

        String content = Files.readString(Paths.get(filename));
        assertTrue(content.contains("=== TEST REPORT ==="));
        assertTrue(content.contains("Line 1"));
        assertTrue(content.contains("Total: 2"));

        Files.delete(Paths.get(filename));
    }
}