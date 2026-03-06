package ru.zvonkov.rbac;

public record Permission(String name, String resource, String description) {
    public Permission {
        ValidationUtils.requireNonEmpty(name, "Permission name");
        ValidationUtils.requireNonEmpty(resource, "Resource");
        ValidationUtils.requireNonEmpty(description, "Description");

        name = name.trim().toUpperCase().replace(" ", "");
        resource = resource.trim().toLowerCase();
    }

    public String format() {
        return name + " на " + resource + " - " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        if (namePattern == null || resourcePattern == null) {
            return false;
        }
        return name.equalsIgnoreCase(namePattern) && resource.equals(resourcePattern.toLowerCase());
    }
}