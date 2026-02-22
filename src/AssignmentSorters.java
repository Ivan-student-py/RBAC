import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

public final class AssignmentSorters {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private AssignmentSorters() {}

    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(assignment -> assignment.user().username());
    }

    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(assignment -> assignment.role().name());
    }

    public static Comparator<RoleAssignment> byAssignmentDate() {
        return (a1, a2) -> {
            try {
                LocalDateTime d1 = LocalDateTime.parse(a1.metadata().assignedAt(), FORMATTER);
                LocalDateTime d2 = LocalDateTime.parse(a2.metadata().assignedAt(), FORMATTER);
                return d1.compareTo(d2);
            } catch (Exception e) {
                return 0;
            }
        };
    }
}