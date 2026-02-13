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

        System.out.println("=== Тестирование завершено ===");
    }
}
