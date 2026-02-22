package main.java;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private final boolean autoRenew;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("ExpiresAt must not be null or empty");
        }
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
            LocalDateTime expireTime = LocalDateTime.parse(expiresAt, FORMATTER);
            return LocalDateTime.now().isAfter(expireTime);
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

    public long getTimeRemaining() {
        try {
            LocalDateTime expireTime = LocalDateTime.parse(expiresAt, FORMATTER);
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(expireTime)) {
                return 0;
            }
            return ChronoUnit.MINUTES.between(now, expireTime);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getExpiresAt() { return expiresAt; }
    public boolean isAutoRenew() { return autoRenew; }
}
