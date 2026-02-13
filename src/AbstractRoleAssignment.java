import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata){
        if (user == null) throw new IllegalArgumentException("User must not be null");
        if (role == null) throw new IllegalArgumentException("Role must not be null");
        if (metadata == null) throw new IllegalArgumentException("Metadata must not be null");

        this.assignmentId = UUID.randomUUID().toString();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() { return assignmentId; }
    @Override
    public User user() { return user; }
    @Override
    public Role role() { return role; }
    @Override
    public AssignmentMetadata metadata() { return metadata; }

    public String summary() {
        return String.format(
                "Assignment[ID=%s, Type=%s, Active=%s]\nUser: %s\nRole: %s\n%s",
                assignmentId,
                assignmentType(),
                isActive(),
                user.format(),
                role.name(),
                metadata.format()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }
}