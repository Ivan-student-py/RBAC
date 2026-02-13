import java.util.Locale;

public record Permission(String name, String resource, String description) {
    public Permission {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission name must not be null or empty");
        }
        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource must not be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description must not be null or empty");
        }

        name = name.trim().toUpperCase().replace(" ", "");
        resource = resource.trim().toLowerCase();
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        if (namePattern == null || resourcePattern == null) {
            return false;
        }
        return name.equalsIgnoreCase(namePattern) && resource.equals(resourcePattern.toLowerCase());
    }
}
