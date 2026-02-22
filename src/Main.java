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

        System.out.println("=== Тестирование завершено ===");
    }
}
