package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

class AssignmentFiltersTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Test
    void byUser_MatchesCorrectUser() {
        User alice = User.validate("alice", "Alice A", "alice@example.com");
        User bob = User.validate("bob", "Bob B", "bob@example.com");
        Role role = new Role("Viewer", "Read-only");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        PermanentAssignment assign = new PermanentAssignment(alice, role, meta);

        var filter = AssignmentFilters.byUser(alice);
        assertTrue(filter.test(assign));

        var filterBob = AssignmentFilters.byUser(bob);
        assertFalse(filterBob.test(assign));
    }

    @Test
    void byUsername_MatchesCorrectUsername() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role role = new Role("Viewer", "Read-only");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        PermanentAssignment assign = new PermanentAssignment(user, role, meta);

        var filter = AssignmentFilters.byUsername("alice");
        assertTrue(filter.test(assign));

        var filterWrong = AssignmentFilters.byUsername("bob");
        assertFalse(filterWrong.test(assign));
    }

    @Test
    void byRole_MatchesCorrectRole() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role viewer = new Role("Viewer", "Read-only");
        Role admin = new Role("Admin", "Full access");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        PermanentAssignment assign = new PermanentAssignment(user, viewer, meta);

        var filter = AssignmentFilters.byRole(viewer);
        assertTrue(filter.test(assign));

        var filterAdmin = AssignmentFilters.byRole(admin);
        assertFalse(filterAdmin.test(assign));
    }

    @Test
    void byRoleName_MatchesCorrectRoleName() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role role = new Role("Viewer", "Read-only");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        PermanentAssignment assign = new PermanentAssignment(user, role, meta);

        var filter = AssignmentFilters.byRoleName("Viewer");
        assertTrue(filter.test(assign));

        var filterWrong = AssignmentFilters.byRoleName("Admin");
        assertFalse(filterWrong.test(assign));
    }

    @Test
    void activeOnly_MatchesActiveAssignments() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role role = new Role("Viewer", "Read-only");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        PermanentAssignment active = new PermanentAssignment(user, role, meta);

        var filter = AssignmentFilters.activeOnly();
        assertTrue(filter.test(active));
    }

    @Test
    void inactiveOnly_SkipsActiveAssignments() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role role = new Role("Viewer", "Read-only");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assign = new PermanentAssignment(user, role, meta);

        var filter = AssignmentFilters.inactiveOnly();
        assertFalse(filter.test(assign),
                "Активное постоянное назначение не должно соответствовать фильтру неактивных");
    }

    @Test
    void byType_Permanent() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role role = new Role("Viewer", "Read-only");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        PermanentAssignment assign = new PermanentAssignment(user, role, meta);

        var filter = AssignmentFilters.byType("PERMANENT");
        assertTrue(filter.test(assign));

        var filterTemp = AssignmentFilters.byType("TEMPORARY");
        assertFalse(filterTemp.test(assign));
    }

    @Test
    void byType_Temporary() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role role = new Role("Viewer", "Read-only");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        String expiresAt = "2030-12-31 23:59";

        TemporaryAssignment assign = new TemporaryAssignment(user, role, meta, expiresAt, false);

        var filter = AssignmentFilters.byType("TEMPORARY");
        assertTrue(filter.test(assign));

        var filterPerm = AssignmentFilters.byType("PERMANENT");
        assertFalse(filterPerm.test(assign));
    }

    @Test
    void assignedBy_MatchesAssigner() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role role = new Role("Viewer", "Read-only");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        PermanentAssignment assign = new PermanentAssignment(user, role, meta);

        var filter = AssignmentFilters.assignedBy("admin");
        assertTrue(filter.test(assign));

        var filterWrong = AssignmentFilters.assignedBy("manager");
        assertFalse(filterWrong.test(assign));
    }

    @Test
    void assignedAfter_MatchesAssignmentsAfterDate() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role role = new Role("Viewer", "Read-only");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        PermanentAssignment assign = new PermanentAssignment(user, role, meta);

        String pastDate = "2000-01-01 00:00";
        String futureDate = "2100-01-01 00:00";

        var filterPast = AssignmentFilters.assignedAfter(pastDate);
        assertTrue(filterPast.test(assign), "Должно соответствовать дате в прошлом");

        var filterFuture = AssignmentFilters.assignedAfter(futureDate);
        assertFalse(filterFuture.test(assign), "Не должно соответствовать дате в будущем");
    }

    @Test
    void expiringBefore_MatchesExpiringTemporaryAssignments() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role role = new Role("Viewer", "Read-only");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        String expiresAt = "2025-12-31 23:59";

        TemporaryAssignment assign = new TemporaryAssignment(user, role, meta, expiresAt, false);

        var filterBefore = AssignmentFilters.expiringBefore("2026-01-01 00:00");
        assertTrue(filterBefore.test(assign));

        var filterAfter = AssignmentFilters.expiringBefore("2024-01-01 00:00");
        assertFalse(filterAfter.test(assign));
    }

    @Test
    void and_CombinesTwoFilters() {
        User alice = User.validate("alice", "Alice A", "alice@example.com");
        Role viewer = new Role("Viewer", "Read-only");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        PermanentAssignment assign = new PermanentAssignment(alice, viewer, meta);

        var filter = AssignmentFilters.byUsername("alice")
                .and(AssignmentFilters.byRoleName("Viewer"));
        assertTrue(filter.test(assign));
    }

    @Test
    void or_CombinesTwoFilters() {
        User alice = User.validate("alice", "Alice A", "alice@example.com");
        Role viewer = new Role("Viewer", "Read-only");
        Role admin = new Role("Admin", "Full access");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        PermanentAssignment assignViewer = new PermanentAssignment(alice, viewer, meta);
        PermanentAssignment assignAdmin = new PermanentAssignment(alice, admin, meta);

        var filter = AssignmentFilters.byRoleName("Viewer")
                .or(AssignmentFilters.byRoleName("Admin"));

        assertTrue(filter.test(assignViewer));
        assertTrue(filter.test(assignAdmin));
    }
}