package ru.zvonkov.rbac;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new ConcurrentHashMap<>();
    private final Map<String, Role> rolesByName = new ConcurrentHashMap<>();

    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }

        // Атомарная операция с двумя мапами — нужна синхронизация
        synchronized (this) {
            if (rolesById.containsKey(role.id())) {
                throw new IllegalArgumentException("Role with ID '" + role.id() + "' already exists");
            }
            if (rolesByName.containsKey(role.name())) {
                throw new IllegalArgumentException("Role with name '" + role.name() + "' already exists");
            }
            rolesById.put(role.id(), role);
            rolesByName.put(role.name(), role);
        }
    }

    @Override
    public boolean remove(Role role) {
        if (role == null) return false;

        // Атомарное удаление из обеих мап
        synchronized (this) {
            Role removedById = rolesById.remove(role.id());
            Role removedByName = rolesByName.remove(role.name());
            return removedById != null && removedByName != null;
        }
    }

    @Override
    public Optional<Role> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(rolesById.get(id.trim()));
    }

    @Override
    public List<Role> findAll() {
        // Возвращаем копию для защиты от внешних модификаций
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        // Атомарная очистка обеих мап
        synchronized (this) {
            rolesById.clear();
            rolesByName.clear();
        }
    }

    public Optional<Role> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(rolesByName.get(name.trim()));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) {
            return findAll();
        }
        // Чтение из ConcurrentHashMap безопасно без синхронизации
        return rolesById.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findByFilterParallel(RoleFilter filter) {
        if (filter == null) {
            return findAll();
        }
        // Параллельная фильтрация
        return rolesById.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        if (sorter == null) {
            throw new IllegalArgumentException("Sorter must not be null");
        }
        return findByFilter(filter).stream()
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return rolesByName.containsKey(name.trim());
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        ValidationUtils.requireNonEmpty(roleName, "Role name");
        if (permission == null) {
            throw new IllegalArgumentException("Permission must not be null");
        }

        Role role = rolesByName.get(roleName.trim());
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' does not exist");
        }
        // Изменение роли не требует синхронизации (объект уже в мапе)
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        ValidationUtils.requireNonEmpty(roleName, "Role name");
        if (permission == null) {
            throw new IllegalArgumentException("Permission must not be null");
        }

        Role role = rolesByName.get(roleName.trim());
        if (role == null) {
            throw new IllegalArgumentException("Role with name '" + roleName + "' does not exist");
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        ValidationUtils.requireNonEmpty(permissionName, "Permission name");
        ValidationUtils.requireNonEmpty(resource, "Resource");
        String cleanName = permissionName.trim();
        String cleanResource = resource.trim();

        return rolesById.values().stream()
                .filter(role -> role.hasPermission(cleanName, cleanResource))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleManager that = (RoleManager) o;
        return Objects.equals(rolesById, that.rolesById) &&
                Objects.equals(rolesByName, that.rolesByName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rolesById, rolesByName);
    }
}