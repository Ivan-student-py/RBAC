package ru.zvonkov.rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
    }

    @Test
    void initialize_CreatesDefaultData() {
        system.initialize();

        assertEquals(1, system.getUserManager().count());
        assertEquals(3, system.getRoleManager().count());
        assertEquals(1, system.getAssignmentManager().count());

        var adminOpt = system.getUserManager().findByUsername("admin");
        assertTrue(adminOpt.isPresent());
        assertEquals("System Administrator", adminOpt.get().fullName());

        var adminRoleOpt = system.getRoleManager().findByName("Admin");
        assertTrue(adminRoleOpt.isPresent());

        var assignments = system.getAssignmentManager().findByUser(adminOpt.get());
        assertEquals(1, assignments.size());
        assertTrue(assignments.get(0).role().name().equals("Admin"));
    }

    @Test
    void generateStatistics_ReturnsFormattedString() {
        system.initialize();

        String stats = system.generateStatistics();

        assertTrue(stats.contains("Пользователей: 1"));
        assertTrue(stats.contains("Ролей: 3"));
        assertTrue(stats.contains("Назначений всего: 1"));
        assertTrue(stats.contains("Среднее количество ролей на пользователя: 1.00"));
        assertTrue(stats.contains("Топ-3 самых популярных ролей"));
        assertTrue(stats.contains("Admin (1 назначений)"));
    }
}