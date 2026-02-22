package ru.zvonkov.rbac;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Тестирование User ===");

        try {
            User user = User.validate("susan_dev", "Susan Johnson", "susan@example.com");
            System.out.println("Успешный вывод: " + user.format());
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        try {
            User.validate("", "Kirill Safonov", "kirill@example.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Поймана ошибка (пустой username): " + e.getMessage());
        }

        try {
            User.validate("stepan", "Stepan Sechenov", "stepan.example.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Поймана ошибка (неверный email): " + e.getMessage());
        }

        try {
            User.validate("al", "Al", "al@example");
        } catch (IllegalArgumentException e) {
            System.out.println("Поймана ошибка (короткий username): " + e.getMessage());
        }

        System.out.println("\n=== Тестирование Permission ===");

        try {
            Permission perm = new Permission("read", "USERS", "Read user data");
            System.out.println("Успех: " + perm.format());
            System.out.println("  Проверка matches: " + perm.matches("READ", "users"));
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        try {
            new Permission("write", "orders", "");
        } catch (IllegalArgumentException e) {
            System.out.println("Поймана ошибка (пустое описание): " + e.getMessage());
        }

        try {
            Permission perm2 = new Permission(" Create ", "PRODUCTS", "Create new product");
            System.out.println("Нормализация: " + perm2.format());
        } catch (Exception e) {
            System.err.println("Неопознанная ошибка: " + e.getMessage());
        }

        System.out.println("\n=== Тестирование Role ===");

        try {
            Permission readUsers = new Permission("read", "users", "Read user data");
            Permission writeUsers = new Permission("write", "users", "Modify user data");

            Role adminRole = new Role("Administrator", "Full system access");
            adminRole.addPermission(readUsers);
            adminRole.addPermission(writeUsers);

            System.out.println(adminRole.format());
            System.out.println("Has READ on users? " + adminRole.hasPermission("READ", "users"));
            System.out.println("Has DELETE on orders? " + adminRole.hasPermission("DELETE", "orders"));

            Role sameRole = new Role("Moderator", "Content moderation");
            System.out.println("Admin equals new Role? " + adminRole.equals(sameRole));
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        System.out.println("\n=== Тестирование AssignmentMetadata ===");

        try {
            AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Initial setup");
            System.out.println(meta1.format());

            AssignmentMetadata meta2 = AssignmentMetadata.now("moderator", null);
            System.out.println(meta2.format());
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        System.out.println("\n=== Тестирование RoleAssignment ===");

        try {
            User user = User.validate("test_user", "Test User", "test@example.com");
            Role role = new Role("Viewer", "Read-only access");
            AssignmentMetadata meta = AssignmentMetadata.now("admin", "Testing");

            PermanentAssignment permAssign = new PermanentAssignment(user, role, meta);
            System.out.println("Permanent Assignment:\n" + permAssign.summary());

            String futureTime = "2026-12-31 23:00";
            TemporaryAssignment tempAssign = new TemporaryAssignment(user, role, meta, futureTime, false);
            System.out.println("\nTemporary Assignment:\n" + tempAssign.summary());
            System.out.println("Time remaining: " + tempAssign.getTimeRemaining() + " minutes");

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }

        System.out.println("\n=== Тестирование UserFilter ===");

        try {
            User alice = User.validate("alice_dev", "Alice Johnson", "alice@example.com");
            User bob = User.validate("bob_smith", "Bob Smith", "bob@company.org");

            UserFilter byExactUsername = UserFilters.byUsername("alice_dev");
            System.out.println("byUsername('alice_dev') для Alice: " + byExactUsername.test(alice));
            System.out.println("byUsername('alice_dev') для Bob: " + byExactUsername.test(bob));

            UserFilter byUsernameContains = UserFilters.byUsernameContains("ALICE");
            System.out.println("byUsernameContains('ALICE'): " + byUsernameContains.test(alice));

            UserFilter byEmail = UserFilters.byEmail("alice@example.com");
            System.out.println("byEmail('alice@example.com'): " + byEmail.test(alice));

            UserFilter byDomain = UserFilters.byEmailDomain("@company.org");
            System.out.println("byEmailDomain('@company.org') для Bob: " + byDomain.test(bob));

            UserFilter byFullName = UserFilters.byFullNameContains("john");
            System.out.println("byFullNameContains('john'): " + byFullName.test(alice));

            UserFilter combined = byExactUsername.and(byEmail);
            System.out.println("Комбинированный фильтр (username AND email): " + combined.test(alice));

            UserFilter nonExistent = UserFilters.byUsername("charlie");
            System.out.println("Фильтр по несуществующему username: " + nonExistent.test(alice));

        } catch (Exception e) {
            System.err.println("Ошибка в тестах UserFilter: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== Тестирование UserFilter завершено ===");

        System.out.println("\n=== Тестирование RoleFilter ===");

        try {
            Permission readUsers = new Permission("read", "users", "Read user data");
            Permission writeUsers = new Permission("write", "users", "Modify user data");
            Role admin = new Role("Administrator", "Full access");
            admin.addPermission(readUsers);
            admin.addPermission(writeUsers);

            Role viewer = new Role("Viewer", "Read-only");
            viewer.addPermission(readUsers);

            RoleFilter byName = RoleFilters.byName("Administrator");
            System.out.println("byName('Administrator'): " + byName.test(admin));

            RoleFilter byNameContains = RoleFilters.byNameContains("VIEW");
            System.out.println("byNameContains('VIEW'): " + byNameContains.test(viewer));

            RoleFilter hasWrite = RoleFilters.hasPermission(writeUsers);
            System.out.println("hasPermission(write): " + hasWrite.test(admin));
            System.out.println("hasPermission(write) for Viewer: " + hasWrite.test(viewer));

            RoleFilter hasReadUsers = RoleFilters.hasPermission("READ", "users");
            System.out.println("hasPermission('READ', 'users'): " + hasReadUsers.test(admin));

            RoleFilter atLeast2 = RoleFilters.hasAtLeastNPermissions(2);
            System.out.println("hasAtLeastNPermissions(2) for Admin: " + atLeast2.test(admin));
            System.out.println("hasAtLeastNPermissions(2) for Viewer: " + atLeast2.test(viewer));

            // Тест: комбинирование
            RoleFilter combined = byName.and(hasWrite);
            System.out.println("Комбинированный фильтр: " + combined.test(admin));

        } catch (Exception e) {
            System.err.println("Ошибка в тестах RoleFilter: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== Тестирование RoleFilter завершено ===");

        System.out.println("\n=== Тестирование AssignmentFilter ===");

        try {
            User user = User.validate("test_user", "Test User", "test@example.com");
            Role role = new Role("Viewer", "Read-only access");
            Permission readPerm = new Permission("read", "data", "Read data");
            role.addPermission(readPerm);
            AssignmentMetadata meta = AssignmentMetadata.now("admin", "Testing");

            PermanentAssignment permAssign = new PermanentAssignment(user, role, meta);
            TemporaryAssignment tempAssign = new TemporaryAssignment(
                    user, role, meta, "2026-12-31 23:59", false
            );

            AssignmentFilter byUser = AssignmentFilters.byUser(user);
            System.out.println("byUser: " + byUser.test(permAssign));

            AssignmentFilter byUsername = AssignmentFilters.byUsername("test_user");
            System.out.println("byUsername: " + byUsername.test(tempAssign));

            AssignmentFilter active = AssignmentFilters.activeOnly();
            System.out.println("activeOnly for Permanent: " + active.test(permAssign));
            System.out.println("activeOnly for Temporary: " + active.test(tempAssign));

            AssignmentFilter permanent = AssignmentFilters.byType("PERMANENT");
            System.out.println("byType('PERMANENT'): " + permanent.test(permAssign));

            AssignmentFilter assignedBy = AssignmentFilters.assignedBy("admin");
            System.out.println("assignedBy('admin'): " + assignedBy.test(permAssign));

            AssignmentFilter expiring = AssignmentFilters.expiringBefore("2030-01-01 00:00");
            System.out.println("expiringBefore(2030): " + expiring.test(tempAssign)); // true
            System.out.println("expiringBefore(2025): " + expiring.test(permAssign)); // false (permanent)

        } catch (Exception e) {
            System.err.println("Ошибка в тестах AssignmentFilter: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== Тестирование AssignmentFilter завершено ===");

        System.out.println("\n=== Тестирование сортировки ===");

        try {
            User alice = User.validate("alice", "Alice A", "alice@example.com");
            User bob = User.validate("bob", "Bob B", "bob@example.com");
            User charlie = User.validate("charlie", "Charlie C", "charlie@example.com");
            java.util.List<User> users = java.util.Arrays.asList(charlie, alice, bob);

            users.sort(UserSorters.byUsername());
            System.out.println("Пользователи по username:");
            users.forEach(u -> System.out.println("   - " + u.username()));

            Role admin = new Role("Admin", "Full access");
            Role viewer = new Role("Viewer", "Read-only");
            Role editor = new Role("Editor", "Edit content");
            editor.addPermission(new Permission("write", "docs", "Write docs"));
            java.util.List<Role> roles = java.util.Arrays.asList(editor, admin, viewer);

            roles.sort(RoleSorters.byPermissionCount());
            System.out.println("Роли по количеству прав:");
            roles.forEach(r -> System.out.println("   - " + r.name() + " (" + r.getPermissions().size() + ")"));

            AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Test");
            AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Test");

            String time1 = "2025-01-01 10:00";
            String time2 = "2025-01-01 09:00";

            PermanentAssignment assign1 = new PermanentAssignment(alice, admin, meta1);
            PermanentAssignment assign2 = new PermanentAssignment(bob, viewer, meta2);

            java.util.List<RoleAssignment> assignments = java.util.Arrays.asList(assign1, assign2);
            assignments.sort(AssignmentSorters.byUsername());
            System.out.println("Назначения по имени пользователя:");
            assignments.forEach(a -> System.out.println("   - " + a.user().username()));

        } catch (Exception e) {
            System.err.println("Ошибка в тестах сортировки: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== Тестирование сортировки завершено ===");

        System.out.println("=== Тестирование завершено ===");
    }
}
