package main.java;

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
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must not be null or empty");
        }
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
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name must not be null or empty");
        }
        String clean = roleName.trim();
        return assignment -> clean.equals(assignment.role().name());
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Type must not be null or empty");
        }
        String clean = type.trim().toUpperCase();
        return assignment -> clean.equals(assignment.assignmentType());
    }

    public static AssignmentFilter assignedBy(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Assigned by username must not be null or empty");
        }
        String clean = username.trim();
        return assignment -> clean.equals(assignment.metadata().assignedBy());
    }

    public static AssignmentFilter assignedAfter(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Date must not be null or empty");
        }
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
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Date must not be null or empty");
        }
        LocalDateTime targetDate = LocalDateTime.parse(dateStr, FORMATTER);
        return assignment -> {
            if (!(assignment instanceof TemporaryAssignment)) {
                return false;
            }
            TemporaryAssignment temp = (TemporaryAssignment) assignment;
            try {
                LocalDateTime expiresAt = LocalDateTime.parse(temp.getExpiresAt(), FORMATTER);
                return expiresAt.isBefore(targetDate);
            } catch (Exception e) {
                return false;
            }
        };
    }
}