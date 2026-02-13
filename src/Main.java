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

        System.out.println("=== Тестирование завершено ===");
    }
}
