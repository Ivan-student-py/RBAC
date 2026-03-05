package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RoleSortersTest {

    @Test
    void byName_SortsRolesAlphabetically() {
        Role admin = new Role("Admin", "Full access");
        Role viewer = new Role("Viewer", "Read-only");
        Role manager = new Role("Manager", "Management");

        List<Role> roles = new ArrayList<>();
        roles.add(viewer);
        roles.add(admin);
        roles.add(manager);

        roles.sort(RoleSorters.byName());

        assertEquals("Admin", roles.get(0).name());
        assertEquals("Manager", roles.get(1).name());
        assertEquals("Viewer", roles.get(2).name());
    }

    @Test
    void byPermissionCount_SortsByNumberOfPermissions() {
        Role admin = new Role("Admin", "Full access");
        admin.addPermission(new Permission("read", "users", "Read users"));
        admin.addPermission(new Permission("write", "users", "Write users"));
        admin.addPermission(new Permission("delete", "users", "Delete users"));

        Role editor = new Role("Editor", "Edit access");
        editor.addPermission(new Permission("read", "docs", "Read docs"));
        editor.addPermission(new Permission("write", "docs", "Write docs"));

        Role viewer = new Role("Viewer", "Read-only");
        viewer.addPermission(new Permission("read", "docs", "Read docs"));

        List<Role> roles = new ArrayList<>();
        roles.add(editor);
        roles.add(viewer);
        roles.add(admin);

        roles.sort(RoleSorters.byPermissionCount());

        assertEquals(1, roles.get(0).getPermissions().size());
        assertEquals(2, roles.get(1).getPermissions().size());
        assertEquals(3, roles.get(2).getPermissions().size());
    }
}