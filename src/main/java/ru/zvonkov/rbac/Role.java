package ru.zvonkov.rbac;

import java.util.*;

public class Role {
    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions;

    public Role(String name, String description) {
        this(name, description, generateId(), new ArrayList<>());
    }

    Role(String name, String description, String id, Collection<Permission> permissions) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name must not be null or empty");
        }
        this.name = name.trim();
        this.description = (description == null || description.trim().isEmpty())
                ? "Без описания"
                : description.trim();
        this.id = id;
        this.permissions = new HashSet<>(permissions);
    }

    private static String generateId() {
        return "r-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public String id() { return id; }
    public String name() { return name; }
    public String description() { return description; }

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
        return permission != null && permissions.contains(permission);
    }

    public boolean hasPermission(String name, String resource) {
        if (name == null || resource == null) return false;
        return permissions.stream()
                .anyMatch(p -> p.matches(name, resource));
    }

    public boolean removePermission(Permission permission) {
        if (permission == null) return false;
        return permissions.remove(permission);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Роль: ").append(name()).append(" (ID: ").append(id()).append(")\n");
        sb.append("Описание: ").append(description()).append("\n");
        sb.append("Права (").append(permissions.size()).append("):\n");
        if (permissions.isEmpty()) {
            sb.append("  Нет прав\n");
        } else {
            for (Permission p : permissions) {
                sb.append("  • ").append(p.format()).append("\n");
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
        return "Role{id='" + id + "', name='" + name + "'}";
    }
}