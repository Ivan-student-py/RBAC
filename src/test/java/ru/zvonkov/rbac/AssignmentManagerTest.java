package ru.zvonkov.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AssignmentManagerTest {
    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private User user;
    private Role role;
    private Permission readPerm;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);

        user = User.validate("alice", "Alice A", "alice@example.com");
        readPerm = new Permission("read", "data", "Read data");
        role = new Role("Viewer", "Read-only");
        role.addPermission(readPerm);

        userManager.add(user);
        roleManager.add(role);
    }

    @Test
    void testAddAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assign = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assign);
        assertEquals(1, assignmentManager.count());
    }

    @Test
    void testPreventDuplicateActiveAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assign1 = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assign1);

        PermanentAssignment assign2 = new PermanentAssignment(user, role, meta);
        assertThrows(IllegalArgumentException.class, () -> {
            assignmentManager.add(assign2);
        });
    }

    @Test
    void testUserHasPermission() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assign = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assign);

        assertTrue(assignmentManager.userHasPermission(user, "READ", "data"));
        assertFalse(assignmentManager.userHasPermission(user, "WRITE", "data"));
    }

    @Test
    void testGetUserPermissions() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assign = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assign);

        var permissions = assignmentManager.getUserPermissions(user);
        assertEquals(1, permissions.size());
        assertTrue(permissions.contains(readPerm));
    }

    @Test
    void testFindByUser() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assign = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assign);

        List<RoleAssignment> assignments = assignmentManager.findByUser(user);
        assertEquals(1, assignments.size());
        assertEquals(role.name(), assignments.get(0).role().name());
    }

    @Test
    void testFindByRole() {
        User bob = User.validate("bob", "Bob B", "bob@example.com");
        userManager.add(bob);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignAlice = new PermanentAssignment(user, role, meta);
        PermanentAssignment assignBob = new PermanentAssignment(bob, role, meta);
        assignmentManager.add(assignAlice);
        assignmentManager.add(assignBob);

        List<RoleAssignment> assignments = assignmentManager.findByRole(role);
        assertEquals(2, assignments.size());
    }

    @Test
    void testGetActiveAssignments() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assign = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assign);

        List<RoleAssignment> active = assignmentManager.getActiveAssignments();
        assertEquals(1, active.size());
        assertTrue(active.get(0).isActive());
    }

    @Test
    void testGetExpiredAssignments() {
        User bob = User.validate("bob", "Bob B", "bob@example.com");
        userManager.add(bob);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment active = new PermanentAssignment(user, role, meta);
        assignmentManager.add(active);

        String pastDate = "2000-01-01 00:00";
        TemporaryAssignment expired = new TemporaryAssignment(bob, role, meta, pastDate, false);
        assignmentManager.add(expired);

        List<RoleAssignment> expiredList = assignmentManager.getExpiredAssignments();
        assertEquals(1, expiredList.size());
        assertFalse(expiredList.get(0).isActive());
    }

    @Test
    void testRevokeAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assign = new PermanentAssignment(user, role, meta);
        assignmentManager.add(assign);

        assignmentManager.revokeAssignment(assign.assignmentId());

        List<RoleAssignment> active = assignmentManager.getActiveAssignments();
        assertEquals(0, active.size());
    }

    @Test
    void testExtendTemporaryAssignment() {
        String expiresAt = "2025-12-31 23:59";
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        TemporaryAssignment assign = new TemporaryAssignment(user, role, meta, expiresAt, false);
        assignmentManager.add(assign);

        String newExpiresAt = "2030-12-31 23:59";
        assignmentManager.extendTemporaryAssignment(assign.assignmentId(), newExpiresAt);

        List<RoleAssignment> active = assignmentManager.getActiveAssignments();
        assertEquals(1, active.size());
    }

    @Test
    void testFindAllWithFilterAndSorter() {
        User bob = User.validate("bob", "Bob B", "bob@example.com");
        userManager.add(bob);

        Role admin = new Role("Admin", "Full access");
        admin.addPermission(new Permission("write", "data", "Write data"));
        roleManager.add(admin);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignViewer = new PermanentAssignment(user, role, meta);
        PermanentAssignment assignAdmin = new PermanentAssignment(bob, admin, meta);
        assignmentManager.add(assignViewer);
        assignmentManager.add(assignAdmin);

        var filter = AssignmentFilters.byRoleName("Viewer");
        List<RoleAssignment> filtered = assignmentManager.findAll(filter, AssignmentSorters.byUsername());
        assertEquals(1, filtered.size());
        assertEquals("Viewer", filtered.get(0).role().name());
    }
}