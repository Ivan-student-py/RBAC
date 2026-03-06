package ru.zvonkov.rbac;

public final class UserFilters {
    private UserFilters(){}

    public static UserFilter byUsername(String username) {
        ValidationUtils.requireNonEmpty(username, "Username");
        String clean = username.trim();
        return user -> clean.equals(user.username());
    }

    public static UserFilter byUsernameContains(String substring) {
        ValidationUtils.requireNonEmpty(substring, "Substring");
        String clean = substring.trim().toLowerCase();
        return user -> user.username().toLowerCase().contains(clean);
    }

    public static UserFilter byEmail(String email) {
        ValidationUtils.requireNonEmpty(email, "Email");
        String clean = email.trim();
        return user -> clean.equals(user.email());
    }

    public static UserFilter byEmailDomain(String domain) {
        ValidationUtils.requireNonEmpty(domain, "Domain");
        String clean = domain.trim().toLowerCase();
        return user -> user.email().toLowerCase().endsWith(clean);
    }

    public static UserFilter byFullNameContains(String substring) {
        ValidationUtils.requireNonEmpty(substring, "Substring");
        String clean = substring.trim().toLowerCase();
        return user -> user.fullName().toLowerCase().contains(clean);
    }

    public static UserFilter byEmailContains(String substring) {
        ValidationUtils.requireNonEmpty(substring, "Substring");
        String clean = substring.trim().toLowerCase();
        return user -> user.email().toLowerCase().contains(clean);
    }
}