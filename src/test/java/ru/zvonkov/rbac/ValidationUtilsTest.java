package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void isValidUsername_ValidInput_ReturnsTrue() {
        assertTrue(ValidationUtils.isValidUsername("alice"));
        assertTrue(ValidationUtils.isValidUsername("user_123"));
        assertTrue(ValidationUtils.isValidUsername("a1b2c3"));
        assertTrue(ValidationUtils.isValidUsername("test_user_name"));
    }

    @Test
    void isValidUsername_InvalidInput_ReturnsFalse() {
        assertFalse(ValidationUtils.isValidUsername(null));
        assertFalse(ValidationUtils.isValidUsername(""));
        assertFalse(ValidationUtils.isValidUsername("ab"));
        assertFalse(ValidationUtils.isValidUsername("a".repeat(21)));
        assertFalse(ValidationUtils.isValidUsername("user name"));
        assertFalse(ValidationUtils.isValidUsername("user@name"));
        assertFalse(ValidationUtils.isValidUsername("user-name"));
    }

    @Test
    void isValidEmail_ValidInput_ReturnsTrue() {
        assertTrue(ValidationUtils.isValidEmail("alice@example.com"));
        assertTrue(ValidationUtils.isValidEmail("user.name@company.co.uk"));
        assertTrue(ValidationUtils.isValidEmail("test_user123@domain.org"));
        assertTrue(ValidationUtils.isValidEmail("a@b.c"));
    }

    @Test
    void isValidEmail_InvalidInput_ReturnsFalse() {
        assertFalse(ValidationUtils.isValidEmail(null));
        assertFalse(ValidationUtils.isValidEmail(""));
        assertFalse(ValidationUtils.isValidEmail("not-an-email"));
        assertFalse(ValidationUtils.isValidEmail("@example.com"));
        assertFalse(ValidationUtils.isValidEmail("user@"));
        assertFalse(ValidationUtils.isValidEmail("user@.com"));
    }

    @Test
    void isValidDate_ValidInput_ReturnsTrue() {
        assertTrue(ValidationUtils.isValidDate("2026-03-06 14:30"));
        assertTrue(ValidationUtils.isValidDate("2000-01-01 00:00"));
        assertTrue(ValidationUtils.isValidDate("2099-12-31 23:59"));
    }

    @Test
    void isValidDate_InvalidInput_ReturnsFalse() {
        assertFalse(ValidationUtils.isValidDate(null));
        assertFalse(ValidationUtils.isValidDate(""));
        assertFalse(ValidationUtils.isValidDate("2026-03-06"));
        assertFalse(ValidationUtils.isValidDate("2026-03-06T14:30"));
        assertFalse(ValidationUtils.isValidDate("2026-03-06 14:30:00"));
        assertFalse(ValidationUtils.isValidDate("06-03-2026 14:30"));
    }

    @Test
    void normalizeString_NormalizesCorrectly() {
        assertEquals("alice", ValidationUtils.normalizeString("Alice"));
        assertEquals("alice", ValidationUtils.normalizeString("ALICE"));
        assertEquals("alice", ValidationUtils.normalizeString(" alice "));
        assertEquals("alice", ValidationUtils.normalizeString("  Alice  "));
        assertEquals("", ValidationUtils.normalizeString(null));
        assertEquals("", ValidationUtils.normalizeString("   "));
    }

    @Test
    void requireNonEmpty_ValidInput_DoesNotThrow() {
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty("test", "field"));
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty(" test ", "field"));
    }

    @Test
    void requireNonEmpty_InvalidInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty(null, "field");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty("", "field");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            ValidationUtils.requireNonEmpty("   ", "field");
        });
    }
}