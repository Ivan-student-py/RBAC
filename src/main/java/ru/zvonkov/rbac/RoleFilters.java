package ru.zvonkov.rbac;

public final class RoleFilters {

    private RoleFilters() {}

    public static RoleFilter byName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name must not be null or empty");
        }
        String clean = name.trim();
        return role -> clean.equals(role.name());
    }

    public static RoleFilter byNameContains(String substring) {
        if (substring == null || substring.trim().isEmpty()) {
            throw new IllegalArgumentException("Substring must not be null or empty");
        }
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
        if (permissionName == null || permissionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission name must not be null or empty");
        }
        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource must not be null or empty");
        }
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
}