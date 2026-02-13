import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    public static User validate(String username, String fullName, String email){
        if (username == null || username.trim().isEmpty()){
            throw new IllegalArgumentException("Username must not be null or empty");
        }
        if (fullName == null || fullName.trim().isEmpty()){
            throw new IllegalArgumentException("Full name must not be null or empty");
        }
        if (email == null || email.trim().isEmpty()){
            throw new IllegalArgumentException("Email must not be null or empty");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()){
            throw new IllegalArgumentException("Username must be 3-20 characters long and contain only letters, digits, or underscore");
        }
        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            throw new IllegalArgumentException("Email must contain '@'");
        }
        String domainPart = email.substring(atIndex + 1);
        if (!domainPart.contains(".")){
            throw new IllegalArgumentException("Email domain must contain a dot (example, user@example.com)");
        }

        return new User(username, fullName, email);
    }
    public String format(){
        return username + " (" + fullName + ") <" + email + ">";
    }
}