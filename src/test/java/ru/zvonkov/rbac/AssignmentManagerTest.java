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
}