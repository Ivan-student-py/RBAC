package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserFiltersTest {

    @Test
    void byUsername_ExactMatch() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        var filter = UserFilters.byUsername("alice");
        assertTrue(filter.test(user));
    }

    @Test
    void byUsername_NoMatch() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        var filter = UserFilters.byUsername("bob");
        assertFalse(filter.test(user));
    }

    @Test
    void byUsernameContains_IgnoreCase() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        var filter = UserFilters.byUsernameContains("ALI");
        assertTrue(filter.test(user));
    }

    @Test
    void byEmail_ExactMatch() {
        User user = User.validate("alice", "Alice A", "alice@example.com");
        var filter = UserFilters.byEmail("alice@example.com");
        assertTrue(filter.test(user));
    }

    @Test
    void byEmailDomain_Matches() {
        User user = User.validate("alice", "Alice A", "alice@company.com");
        var filter = UserFilters.byEmailDomain("@company.com");
        assertTrue(filter.test(user));
    }

    @Test
    void byEmailDomain_NoMatch() {
        User user = User.validate("alice", "Alice A", "alice@gmail.com");
        var filter = UserFilters.byEmailDomain("@company.com");
        assertFalse(filter.test(user));
    }

    @Test
    void byFullNameContains_Substring() {
        User user = User.validate("alice", "Alice Smith", "alice@example.com");
        var filter = UserFilters.byFullNameContains("Smith");
        assertTrue(filter.test(user));
    }

    @Test
    void and_CombinesFilters() {
        User user = User.validate("alice", "Alice A", "alice@company.com");
        var filter = UserFilters.byUsername("alice")
                .and(UserFilters.byEmailDomain("@company.com"));
        assertTrue(filter.test(user));
    }

    @Test
    void or_CombinesFilters() {
        User user = User.validate("alice", "Alice A", "alice@company.com");
        var filter = UserFilters.byUsername("bob")
                .or(UserFilters.byEmailDomain("@company.com"));
        assertTrue(filter.test(user));
    }
}