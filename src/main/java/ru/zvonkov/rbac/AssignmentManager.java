package ru.zvonkov.rbac;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments = new ConcurrentHashMap<>();
    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        if (userManager == null || roleManager == null) {
            throw new IllegalArgumentException("UserManager and RoleManager must not be null");
        }
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment must not be null");
        }

        // Проверка существования пользователя и роли (безопасно — менеджеры потокобезопасны)
        if (!userManager.exists(assignment.user().username())) {
            throw new IllegalArgumentException("User does not exist: " + assignment.user().username());
        }

        if (!roleManager.exists(assignment.role().name())) {
            throw new IllegalArgumentException("Role does not exist: " + assignment.role().name());
        }

        // Критическая секция: проверка дубликатов + вставка должна быть атомарной
        synchronized (this) {
            boolean duplicateActive = assignments.values().stream()
                    .anyMatch(a -> a.isActive() &&
                            a.user().username().equals(assignment.user().username()) &&
                            a.role().name().equals(assignment.role().name()));
            if (duplicateActive) {
                throw new IllegalArgumentException("Active assignment already exists for user '" +
                        assignment.user().username() + "' and role '" + assignment.role().name() + "'");
            }

            assignments.put(assignment.assignmentId(), assignment);
        }
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) return false;
        return assignments.remove(assignment.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(assignments.get(id.trim()));
    }

    @Override
    public List<RoleAssignment> findAll() {
        // Возвращаем копию для защиты от внешних модификаций
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        // Атомарная очистка
        synchronized (this) {
            assignments.clear();
        }
    }

    public List<RoleAssignment> findByUser(User user) {
        if (user == null) return Collections.emptyList();
        // Чтение из ConcurrentHashMap безопасно без синхронизации
        return assignments.values().stream()
                .filter(a -> a.user().username().equals(user.username()))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        if (role == null) return Collections.emptyList();
        return assignments.values().stream()
                .filter(a -> a.role().name().equals(role.name()))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) return findAll();
        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        if (sorter == null) {
            throw new IllegalArgumentException("Sorter must not be null");
        }
        return findByFilter(filter).stream()
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignments.values().stream()
                .filter(a -> !a.isActive())
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        if (user == null || role == null) return false;
        return assignments.values().stream()
                .anyMatch(a -> a.isActive() &&
                        a.user().username().equals(user.username()) &&
                        a.role().name().equals(role.name()));
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        if (user == null || permissionName == null || resource == null) return false;
        return getUserPermissions(user).stream()
                .anyMatch(p -> p.matches(permissionName, resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        if (user == null) return Collections.emptySet();
        // Собираем права из всех активных назначений пользователя
        return assignments.values().stream()
                .filter(a -> a.isActive() && a.user().username().equals(user.username()))
                .flatMap(a -> a.role().getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public void revokeAssignment(String assignmentId) {
        ValidationUtils.requireNonEmpty(assignmentId, "Assignment ID");
        RoleAssignment assignment = assignments.get(assignmentId.trim());
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment not found: " + assignmentId);
        }
        if (assignment instanceof PermanentAssignment) {
            // Изменение состояния назначения — не требует синхронизации мапы
            ((PermanentAssignment) assignment).revoke();
        } else {
            throw new IllegalArgumentException("Only permanent assignments can be revoked directly");
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        ValidationUtils.requireNonEmpty(assignmentId, "Assignment ID");
        ValidationUtils.requireNonEmpty(newExpirationDate, "New expiration date");
        RoleAssignment assignment = assignments.get(assignmentId.trim());
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment not found: " + assignmentId);
        }
        if (assignment instanceof TemporaryAssignment) {
            // Изменение состояния временного назначения
            ((TemporaryAssignment) assignment).extendMinutesUntil(newExpirationDate.trim());
        } else {
            throw new IllegalArgumentException("Only temporary assignments can be extended");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentManager that = (AssignmentManager) o;
        return Objects.equals(assignments, that.assignments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignments);
    }
}