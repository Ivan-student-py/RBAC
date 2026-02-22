package ru.zvonkov.rbac;

import java.util.Comparator;

public final class RoleSorters {

    private RoleSorters() {}

    public static Comparator<Role> byName() {
        return Comparator.comparing(Role::name);
    }

    public static Comparator<Role> byPermissionCount() {
        return Comparator.comparing(role -> role.getPermissions().size());
    }
}