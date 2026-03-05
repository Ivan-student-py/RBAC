package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleFiltersTest {

    @Test
    void byName_ExactMatch() {
        Role role = new Role("Admin", "Full access");
        var filter = RoleFilters.byName("Admin");
        assertTrue(filter.test(role));
    }

    @Test
    void byName_NoMatch() {
        Role role = new Role("Admin", "Full access");
        var filter = RoleFilters.byName("Viewer");
        assertFalse(filter.test(role));
    }

    @Test
    void byNameContains_SubstringIgnoreCase() {
        Role role = new Role("SystemAdministrator", "Full access");
        var filter = RoleFilters.byNameContains("ADMIN");
        assertTrue(filter.test(role));
    }

    @Test
    void byNameContains_NoMatch() {
        Role role = new Role("Viewer", "Read-only");
        var filter = RoleFilters.byNameContains("Admin");
        assertFalse(filter.test(role));
    }

    @Test
    void hasPermission_ByPermissionObject() {
        Permission read = new Permission("read", "users", "Read users");
        Role role = new Role("Viewer", "Read-only");
        role.addPermission(read);

        var filter = RoleFilters.hasPermission(read);
        assertTrue(filter.test(role));
    }

    @Test
    void hasPermission_ByPermissionObject_NoMatch() {
        Permission write = new Permission("write", "users", "Write users");
        Role role = new Role("Viewer", "Read-only");

        var filter = RoleFilters.hasPermission(write);
        assertFalse(filter.test(role));
    }

    @Test
    void hasPermission_ByStringNameAndResource() {
        Permission read = new Permission("read", "users", "Read users");
        Role role = new Role("Viewer", "Read-only");
        role.addPermission(read);

        var filter = RoleFilters.hasPermission("READ", "users");
        assertTrue(filter.test(role));
    }

    @Test
    void hasPermission_ByStringNameAndResource_NoMatch() {
        Role role = new Role("Viewer", "Read-only");

        var filter = RoleFilters.hasPermission("WRITE", "users");
        assertFalse(filter.test(role));
    }

    @Test
    void hasAtLeastNPermissions_ExactCount() {
        Role role = new Role("Editor", "Edit access");
        role.addPermission(new Permission("read", "docs", "Read docs"));
        role.addPermission(new Permission("write", "docs", "Write docs"));

        var filter = RoleFilters.hasAtLeastNPermissions(2);
        assertTrue(filter.test(role));
    }

    @Test
    void hasAtLeastNPermissions_NotEnough() {
        Role role = new Role("Viewer", "Read-only");
        role.addPermission(new Permission("read", "docs", "Read docs"));

        var filter = RoleFilters.hasAtLeastNPermissions(2);
        assertFalse(filter.test(role));
    }

    @Test
    void and_CombinesTwoFilters() {
        Role role = new Role("Admin", "Full access");
        role.addPermission(new Permission("read", "users", "Read users"));
        role.addPermission(new Permission("write", "users", "Write users"));

        var filter = RoleFilters.byName("Admin")
                .and(RoleFilters.hasPermission("READ", "users"));
        assertTrue(filter.test(role));
    }

    @Test
    void or_CombinesTwoFilters() {
        Role role1 = new Role("Admin", "Full access");
        Role role2 = new Role("Viewer", "Read-only");
        role2.addPermission(new Permission("read", "users", "Read users"));

        var filter = RoleFilters.byName("Admin")
                .or(RoleFilters.hasPermission("READ", "users"));

        assertTrue(filter.test(role1));
        assertTrue(filter.test(role2));
    }
}