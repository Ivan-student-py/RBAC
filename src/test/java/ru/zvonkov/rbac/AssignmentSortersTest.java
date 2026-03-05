package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AssignmentSortersTest {

    @Test
    void byUsername_SortsAssignmentsAlphabeticallyByUser() {
        User alice = User.validate("alice", "Alice A", "alice@example.com");
        User bob = User.validate("bob", "Bob B", "bob@example.com");
        User charlie = User.validate("charlie", "Charlie C", "charlie@example.com");
        Role role = new Role("Viewer", "Read-only");

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignBob = new PermanentAssignment(bob, role, meta);
        PermanentAssignment assignAlice = new PermanentAssignment(alice, role, meta);
        PermanentAssignment assignCharlie = new PermanentAssignment(charlie, role, meta);

        List<RoleAssignment> assignments = new ArrayList<>();
        assignments.add(assignBob);
        assignments.add(assignAlice);
        assignments.add(assignCharlie);

        assignments.sort(AssignmentSorters.byUsername());

        assertEquals("alice", assignments.get(0).user().username());
        assertEquals("bob", assignments.get(1).user().username());
        assertEquals("charlie", assignments.get(2).user().username());
    }

    @Test
    void byRoleName_SortsAssignmentsAlphabeticallyByRole() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role admin = new Role("Admin", "Full access");
        Role manager = new Role("Manager", "Management");
        Role viewer = new Role("Viewer", "Read-only");

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignViewer = new PermanentAssignment(user, viewer, meta);
        PermanentAssignment assignAdmin = new PermanentAssignment(user, admin, meta);
        PermanentAssignment assignManager = new PermanentAssignment(user, manager, meta);

        List<RoleAssignment> assignments = new ArrayList<>();
        assignments.add(assignViewer);
        assignments.add(assignAdmin);
        assignments.add(assignManager);

        assignments.sort(AssignmentSorters.byRoleName());

        assertEquals("Admin", assignments.get(0).role().name());
        assertEquals("Manager", assignments.get(1).role().name());
        assertEquals("Viewer", assignments.get(2).role().name());
    }

    @Test
    void byAssignmentDate_SortsAssignmentsChronologically() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        Role role = new Role("Viewer", "Read-only");

        // Создаём назначения с разными датами
        AssignmentMetadata meta1 = new AssignmentMetadata("admin", "2020-01-01 10:00", "Test");
        AssignmentMetadata meta2 = new AssignmentMetadata("admin", "2021-05-15 14:30", "Test");
        AssignmentMetadata meta3 = new AssignmentMetadata("admin", "2022-12-31 23:59", "Test");

        PermanentAssignment assignOld = new PermanentAssignment(user, role, meta1);
        PermanentAssignment assignMiddle = new PermanentAssignment(user, role, meta2);
        PermanentAssignment assignNew = new PermanentAssignment(user, role, meta3);

        List<RoleAssignment> assignments = new ArrayList<>();
        assignments.add(assignMiddle);
        assignments.add(assignNew);
        assignments.add(assignOld);

        assignments.sort(AssignmentSorters.byAssignmentDate());

        assertEquals("2020-01-01 10:00", assignments.get(0).metadata().assignedAt());
        assertEquals("2021-05-15 14:30", assignments.get(1).metadata().assignedAt());
        assertEquals("2022-12-31 23:59", assignments.get(2).metadata().assignedAt());
    }
}