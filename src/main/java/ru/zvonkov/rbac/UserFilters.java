package ru.zvonkov.rbac;

public final class UserFilters {
    private UserFilters(){}

    public static UserFilter byUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username must not be null or empty");
        }
        String clean = username.trim();
        return user -> clean.equals(user.username());
    }

    public static UserFilter byUsernameContains(String substring) {
        if (substring == null || substring.trim().isEmpty()) {
            throw new IllegalArgumentException("Substring must not be null or empty");
        }
        String clean = substring.trim().toLowerCase();
        return user -> user.username().toLowerCase().contains(clean);
    }

    public static UserFilter byEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }
        String clean = email.trim();
        return user -> clean.equals(user.email());
    }

    public static UserFilter byEmailDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain must not be null or empty");
        }
        String clean = domain.trim().toLowerCase();
        return user -> user.email().toLowerCase().endsWith(clean);
    }

    public static UserFilter byFullNameContains(String substring) {
        if (substring == null || substring.trim().isEmpty()) {
            throw new IllegalArgumentException("Substring must not be null or empty");
        }
        String clean = substring.trim().toLowerCase();
        return user -> user.fullName().toLowerCase().contains(clean);
    }
}