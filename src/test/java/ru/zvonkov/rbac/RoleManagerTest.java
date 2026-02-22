package ru.zvonkov.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class RoleManagerTest {
    private RoleManager manager;
    private Role admin;
    private Role viewer;
    private Permission readPerm;
    private Permission writePerm;

    @BeforeEach
    void setUp() {
        manager = new RoleManager();
        readPerm = new Permission("read", "data", "Read data");
        writePerm = new Permission("write", "data", "Write data");
        admin = new Role("Admin", "Full access");
        viewer = new Role("Viewer", "Read-only");
        admin.addPermission(readPerm);
        admin.addPermission(writePerm);
        viewer.addPermission(readPerm);
    }

    @Test
    void testAddRole() {
        manager.add(admin);
        assertEquals(1, manager.count());
        assertTrue(manager.exists("Admin"));
    }

    @Test
    void testAddDuplicateRoleByName() {
        manager.add(admin);
        Role another = new Role("Admin", "Another admin");
        assertThrows(IllegalArgumentException.class, () -> {
            manager.add(another);
        });
    }

    @Test
    void testFindByName() {
        manager.add(viewer);
        Optional<Role> found = manager.findByName("Viewer");
        assertTrue(found.isPresent());
        assertEquals("Read-only", found.get().description());
    }

    @Test
    void testFindByFilter() {
        manager.add(admin);
        manager.add(viewer);
        List<Role> filtered = manager.findByFilter(RoleFilters.hasPermission("read", "data"));
        assertEquals(2, filtered.size());
    }

    @Test
    void testAddPermissionToRole() {
        manager.add(viewer);
        manager.addPermissionToRole("Viewer", writePerm);
        assertTrue(manager.findByName("Viewer").get().hasPermission(writePerm));
    }

    @Test
    void testFindRolesWithPermission() {
        manager.add(admin);
        manager.add(viewer);
        List<Role> roles = manager.findRolesWithPermission("write", "data");
        assertEquals(1, roles.size());
        assertEquals("Admin", roles.get(0).name());
    }

    @Test
    void testRemoveRole() {
        manager.add(admin);
        assertTrue(manager.remove(admin));
        assertEquals(0, manager.count());
        assertFalse(manager.exists("Admin"));
    }

    @Test
    void testFindById() {
        manager.add(admin);
        Optional<Role> found = manager.findById(admin.id());
        assertTrue(found.isPresent());
        assertEquals("Admin", found.get().name());
    }
}