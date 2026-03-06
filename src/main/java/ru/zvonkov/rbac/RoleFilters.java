package ru.zvonkov.rbac;

public final class RoleFilters {

    private RoleFilters() {}

    public static RoleFilter byName(String name) {
        ValidationUtils.requireNonEmpty(name, "Role name");
        String clean = name.trim();
        return role -> clean.equals(role.name());
    }

    public static RoleFilter byNameContains(String substring) {
        ValidationUtils.requireNonEmpty(substring, "Substring");
        String clean = substring.trim().toLowerCase();
        return role -> role.name().toLowerCase().contains(clean);
    }

    public static RoleFilter hasPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission must not be null");
        }
        return role -> role.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        ValidationUtils.requireNonEmpty(permissionName, "Permission name");
        ValidationUtils.requireNonEmpty(resource, "Resource");
        String cleanName = permissionName.trim();
        String cleanResource = resource.trim();
        return role -> role.hasPermission(cleanName, cleanResource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Number of permissions must be non-negative");
        }
        return role -> role.getPermissions().size() >= n;
    }

    public static RoleFilter minPermissions(int minCount) {
        if (minCount < 0) {
            throw new IllegalArgumentException("Минимальное количество прав не может быть отрицательным");
        }
        return role -> role.getPermissions().size() >= minCount;
    }
}