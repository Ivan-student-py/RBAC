package main.java;

import java.util.*;

public class Role {
    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions;

    public Role(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name must not be null or empty");
        }
        if (description == null || description.trim().isEmpty()){
            throw new IllegalArgumentException("Role description must not be null or empty");
        }

        this.id = UUID.randomUUID().toString();
        this.name = name.trim();
        this.description = description.trim();
        this.permissions = new HashSet<>();
    }

    public String id() { return id; }
    public String name() {return name; }
    public String description() {return description; }
    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public boolean addPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permission must not be null");
        }
        return permissions.add(permission);
    }
    public boolean hasPermission(Permission permission) {
        if (permission == null) {
            return false;
        }
        return permissions.contains(permission);
    }

    public boolean hasPermission(String name, String resource) {
        if (name == null || resource == null) {
            return false;
        }
        return permissions.stream()
                .anyMatch(p -> p.matches(name, resource));
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Role: ").append(name).append("\n");
        sb.append("ID: ").append(id).append("\n");
        sb.append("Description: ").append(description).append("\n");
        sb.append("Permissions: \n");
        if (permissions.isEmpty()) {
            sb.append("  (no permissions)\n");
        } else {
            for (Permission p : permissions) {
                sb.append("  - ").append(p.format()).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Role{id='" + "', name='" + name + "'}";
    }
}
