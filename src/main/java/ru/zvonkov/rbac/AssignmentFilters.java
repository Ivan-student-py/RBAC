package ru.zvonkov.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AssignmentFilters {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private AssignmentFilters() {}

    public static AssignmentFilter byUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        return assignment -> assignment.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        ValidationUtils.requireNonEmpty(username, "Username");
        String clean = username.trim();
        return assignment -> clean.equals(assignment.user().username());
    }

    public static AssignmentFilter byRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
        return assignment -> assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        ValidationUtils.requireNonEmpty(roleName, "Role name");
        String clean = roleName.trim();
        return assignment -> clean.equals(assignment.role().name());
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byStatus(boolean active) {
        return active ? activeOnly() : inactiveOnly();
    }

    public static AssignmentFilter byType(String type) {
        ValidationUtils.requireNonEmpty(type, "Type");
        String clean = type.trim().toUpperCase();
        return assignment -> clean.equals(assignment.assignmentType());
    }

    public static AssignmentFilter assignedBy(String username) {
        ValidationUtils.requireNonEmpty(username, "Assigned by username");
        String clean = username.trim();
        return assignment -> clean.equals(assignment.metadata().assignedBy());
    }

    public static AssignmentFilter assignedAfter(String dateStr) {
        ValidationUtils.requireNonEmpty(dateStr, "Date");
        LocalDateTime targetDate = LocalDateTime.parse(dateStr, FORMATTER);
        return assignment -> {
            try {
                LocalDateTime assignedAt = LocalDateTime.parse(assignment.metadata().assignedAt(), FORMATTER);
                return assignedAt.isAfter(targetDate);
            } catch (Exception e) {
                return false;
            }
        };
    }

    public static AssignmentFilter expiringBefore(String dateStr) {
        ValidationUtils.requireNonEmpty(dateStr, "Date");
        LocalDateTime targetDate = LocalDateTime.parse(dateStr, FORMATTER);
        return assignment -> {
            if (!(assignment instanceof TemporaryAssignment)) {
                return false;
            }
            TemporaryAssignment temp = (TemporaryAssignment) assignment;
            try {
                LocalDateTime expiresAt = LocalDateTime.parse(temp.expiresAt(), FORMATTER);
                return expiresAt.isBefore(targetDate);
            } catch (Exception e) {
                return false;
            }
        };
    }
}