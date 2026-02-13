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

        System.out.println("=== Тестирование завершено ===");
    }
}
