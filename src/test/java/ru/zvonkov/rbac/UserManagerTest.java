package ru.zvonkov.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {
    private UserManager manager;
    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        manager = new UserManager();
        alice = User.validate("alice", "Alice A", "alice@example.com");
        bob = User.validate("bob", "Bob B", "bob@example.com");
    }

    @Test
    void testAddUser() {
        manager.add(alice);
        assertEquals(1, manager.count());
        assertTrue(manager.exists("alice"));
    }

    @Test
    void testAddDuplicateUser() {
        manager.add(alice);
        assertThrows(IllegalArgumentException.class, () -> {
            manager.add(alice);
        });
    }

    @Test
    void testFindByUsername() {
        manager.add(alice);
        Optional<User> found = manager.findByUsername("alice");
        assertTrue(found.isPresent());
        assertEquals("Alice A", found.get().fullName());
    }

    @Test
    void testFindByEmail() {
        manager.add(bob);
        Optional<User> found = manager.findByEmail("bob@example.com");
        assertTrue(found.isPresent());
        assertEquals("bob", found.get().username());
    }

    @Test
    void testUpdateUser() {
        manager.add(alice);
        manager.update("alice", "Alice Updated", "alice_new@example.com");
        User updated = manager.findByUsername("alice").get();
        assertEquals("Alice Updated", updated.fullName());
        assertEquals("alice_new@example.com", updated.email());
    }

    @Test
    void testUpdateNonExistentUser() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.update("charlie", "Charlie", "c@example.com");
        });
    }

    @Test
    void testFindByFilter() {
        manager.add(alice);
        manager.add(bob);
        List<User> filtered = manager.findByFilter(UserFilters.byUsernameContains("ALICE"));
        assertEquals(1, filtered.size());
        assertEquals("alice", filtered.get(0).username());
    }

    @Test
    void testFindAllWithSorter() {
        manager.add(bob);
        manager.add(alice);
        List<User> sorted = manager.findAll(null, UserSorters.byUsername());
        assertEquals("alice", sorted.get(0).username());
        assertEquals("bob", sorted.get(1).username());
    }

    @Test
    void testRemoveUser() {
        manager.add(alice);
        assertTrue(manager.remove(alice));
        assertEquals(0, manager.count());
    }

    @Test
    void testFindByIdReturnsEmpty() {
        Optional<User> result = manager.findById("any-id");
        assertFalse(result.isPresent());
    }
}