package ru.zvonkov.rbac;

public record User(String username, String fullName, String email) {

    public static User validate(String username, String fullName, String email) {
        ValidationUtils.requireNonEmpty(username, "Username");
        ValidationUtils.requireNonEmpty(fullName, "Full name");
        ValidationUtils.requireNonEmpty(email, "Email");

        if (!ValidationUtils.isValidUsername(username)) {
            throw new IllegalArgumentException(
                    "Username must be 3-20 characters long and contain only letters, digits, or underscore"
            );
        }

        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException(
                    "Email must be in valid format (example: user@example.com)"
            );
        }

        return new User(username.trim(), fullName.trim(), email.trim());
    }

    public String format() {
        return username + " (" + fullName + ") <" + email + ">";
    }
}