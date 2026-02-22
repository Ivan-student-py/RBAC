package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    @Test
    void testValidUser() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        assertEquals("alice", user.username());
        assertEquals("Alice A", user.fullName());
        assertEquals("alice@example.com", user.email());
    }

    @Test
    void testInvalidUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            User.validate("", "Alice", "alice@example.com");
        });
    }
}
