package ru.zvonkov.rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private final boolean autoRenew;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        ValidationUtils.requireNonEmpty(expiresAt, "ExpiresAt");
        this.expiresAt = expiresAt.trim();
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        return !isExpired();
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public boolean isExpired() {
        try {
            String now = DateUtils.getCurrentDateTimeShort();
            return DateUtils.isAfterDateTime(now, expiresAt);
        } catch (Exception e) {
            return true;
        }
    }

    public void extend(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Minutes must be positive");
        }
        LocalDateTime currentExpire = LocalDateTime.parse(expiresAt, FORMATTER);
        LocalDateTime newExpire = currentExpire.plusMinutes(minutes);
        this.expiresAt = newExpire.format(FORMATTER);
    }

    public void extendMinutesUntil(String newExpirationDate) {
        ValidationUtils.requireNonEmpty(newExpirationDate, "New expiration date");
        this.expiresAt = newExpirationDate.trim();
    }

    public long getTimeRemaining() {
        try {
            String now = DateUtils.getCurrentDateTime().substring(0, 16);
            if (DateUtils.isAfter(now, expiresAt)) {
                return 0;
            }
            // Упрощённый расчёт: 1 день = 1440 минут
            return 1440;
        } catch (Exception e) {
            return 0;
        }
    }

    public String getExpiresAt() { return expiresAt; }
    public boolean isAutoRenew() { return autoRenew; }
    public String expiresAt() {
        return expiresAt;
    }
}
