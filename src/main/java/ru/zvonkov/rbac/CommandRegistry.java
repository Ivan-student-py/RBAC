package ru.zvonkov.rbac;

import java.util.Scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CommandRegistry {
    public static void registerAllCommands(CommandParser parser) {
        // === Служебные команды ===
        registerHelp(parser);
        registerStats(parser);
        registerClear(parser);
        registerExit(parser);

        // === Команды управления пользователями ===
        registerUserList(parser);
        registerUserCreate(parser);
        registerUserView(parser);
        registerUserUpdate(parser);
        registerUserDelete(parser);
        registerUserSearch(parser);

        // === Команды управления ролями ===
        registerRoleList(parser);

        // === Команды управления назначениями ===

        // === Команды просмотра прав ===
    }

    // --- Служебные команды ---
    private static void registerHelp(CommandParser parser) {
        parser.registerCommand("help", "Показать справку по командам", (scanner, system) -> {
            parser.printHelp();
        });
    }

    private static void registerStats(CommandParser parser) {
        parser.registerCommand("stats", "Показать статистику системы", (scanner, system) -> {
            System.out.println(system.generateStatistics());
        });
    }

    private static void registerClear(CommandParser parser) {
        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        });
    }

    private static void registerExit(CommandParser parser) {
        parser.registerCommand("exit", "Выйти из программы", (scanner, system) -> {
            // обработка команды в Main.java
        });
    }

    private static void registerUserList(CommandParser parser) {
        parser.registerCommand("user-list", "Вывести список всех пользователей (с фильтрацией)", (scanner, system) -> {
            System.out.println("\n=== Список пользователей ===");
            List<User> users = system.getUserManager().findAll();
            if (users.isEmpty()) {
                System.out.println("Нет зарегистрированных пользователей.");
                return;
            }

            System.out.printf("%-20s | %-25s | %s%n", "Username", "Full Name", "Email");
            System.out.println("-".repeat(70));
            for (User user : users) {
                System.out.printf("%-20s | %-25s | %s%n",
                        user.username(),
                        user.fullName(),
                        user.email());
            }
            System.out.println("=".repeat(70));
        });
    }

    private static void registerUserCreate(CommandParser parser) {
        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            System.out.println("\n=== Создание нового пользователя ===");

            try {
                System.out.print("Введите username (3–20 символов, буквы, цифры, _): ");
                String username = scanner.nextLine().trim();
                if (username.isEmpty()) {
                    System.out.println("Ошибка: username не может быть пустым.");
                    return;
                }

                System.out.print("Введите полное имя: ");
                String fullName = scanner.nextLine().trim();
                if (fullName.isEmpty()) {
                    System.out.println("Ошибка: полное имя не может быть пустым.");
                    return;
                }

                System.out.print("Введите email: ");
                String email = scanner.nextLine().trim();
                if (email.isEmpty()) {
                    System.out.println("Ошибка: email не может быть пустым.");
                    return;
                }

                User user = User.validate(username, fullName, email);
                system.getUserManager().add(user);

                System.out.println("Пользователь успешно создан:");
                System.out.println(user.format());

            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при создании пользователя: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage());
            }
        });
    }

    private static void registerUserView(CommandParser parser) {
        parser.registerCommand("user-view", "Просмотр информации о пользователе", (scanner, system) -> {
            System.out.print("\nВведите username пользователя: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username не может быть пустым.");
                return;
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь с username '" + username + "' не найден.");
                return;
            }

            User user = userOpt.get();
            System.out.println("\n=== Информация о пользователе ===");
            System.out.println(user.format());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("\nНет назначенных ролей.");
            } else {
                System.out.println("\nНазначенные роли:");
                for (RoleAssignment assignment : assignments) {
                    System.out.println("  - " + assignment.role().name() +
                            " (" + (assignment.isActive() ? "активно" : "неактивно") + ")");
                }
            }

            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);
            if (permissions.isEmpty()) {
                System.out.println("\nНет прав доступа.");
            } else {
                System.out.println("\nПрава доступа (всего: " + permissions.size() + "):");
                Map<String, List<Permission>> byResource = new HashMap<>();
                for (Permission p : permissions) {
                    byResource.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p);
                }

                for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                    System.out.println("  Ресурс: " + entry.getKey());
                    for (Permission p : entry.getValue()) {
                        System.out.println("    • " + p.name() + " — " + p.description());
                    }
                }
            }
            System.out.println("==================================");
        });
    }

    private static void registerUserUpdate(CommandParser parser) {
        parser.registerCommand("user-update", "Обновить данные пользователя", (scanner, system) -> {
            System.out.print("\nВведите username пользователя для обновления: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username не может быть пустым.");
                return;
            }

            Optional<User> existing = system.getUserManager().findByUsername(username);
            if (existing.isEmpty()) {
                System.out.println("Пользователь с username '" + username + "' не найден.");
                return;
            }

            User current = existing.get();
            System.out.println("Текущие данные:");
            System.out.println("  Полное имя: " + current.fullName());
            System.out.println("  Email: " + current.email());

            try {
                System.out.print("Введите новое полное имя (оставьте пустым, чтобы оставить без изменений): ");
                String newFullName = scanner.nextLine().trim();
                if (newFullName.isEmpty()) {
                    newFullName = current.fullName();
                }

                System.out.print("Введите новый email (оставьте пустым, чтобы оставить без изменений): ");
                String newEmail = scanner.nextLine().trim();
                if (newEmail.isEmpty()) {
                    newEmail = current.email();
                }

                system.getUserManager().update(username, newFullName, newEmail);

                System.out.println("Данные пользователя успешно обновлены:");
                Optional<User> updated = system.getUserManager().findByUsername(username);
                updated.ifPresent(u -> System.out.println(u.format()));

            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при обновлении: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage());
            }
        });
    }

    private static void registerUserDelete(CommandParser parser) {
        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            System.out.print("\nВведите username пользователя для удаления: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username не может быть пустым.");
                return;
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь с username '" + username + "' не найден.");
                return;
            }

            User user = userOpt.get();
            System.out.println("Вы собираетесь удалить пользователя:");
            System.out.println(user.format());

            // Получение всех назначений пользователя
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (!assignments.isEmpty()) {
                System.out.println("\n У пользователя есть " + assignments.size() + " назначений(е/ий).");
                System.out.println("Все назначения будут удалены автоматически.");
            }

            System.out.print("\nПодтвердите удаление (введите \"да\"): ");
            String confirm = scanner.nextLine().trim();
            if (!"да".equals(confirm)) {
                System.out.println("Удаление отменено.");
                return;
            }

            try {
                for (RoleAssignment assignment : assignments) {
                    system.getAssignmentManager().remove(assignment);
                }

                boolean removed = system.getUserManager().remove(user);
                if (removed) {
                    System.out.println("Пользователь и все его назначения успешно удалены.");
                } else {
                    System.out.println("Не удалось удалить пользователя.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка при удалении: " + e.getMessage());
            }
        });
    }

    private static void registerUserSearch(CommandParser parser) {
        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (scanner, system) -> {
            System.out.println("\n=== Поиск пользователей ===");
            System.out.println("Выберите тип фильтра:");
            System.out.println("1. По username (содержит)");
            System.out.println("2. По email (содержит)");
            System.out.println("3. По домену email (например, @example.com)");
            System.out.println("4. По полному имени (содержит)");
            System.out.print("Введите номер фильтра (1–4): ");

            String choice = scanner.nextLine().trim();
            UserFilter filter = null;

            switch (choice) {
                case "1":
                    System.out.print("Введите подстроку для поиска в username: ");
                    String substr1 = scanner.nextLine().trim();
                    if (!substr1.isEmpty()) {
                        filter = UserFilters.byUsernameContains(substr1);
                    }
                    break;
                case "2":
                    System.out.print("Введите подстроку для поиска в email: ");
                    String substr2 = scanner.nextLine().trim();
                    if (!substr2.isEmpty()) {
                        filter = UserFilters.byEmailContains(substr2); // ← нужно добавить!
                    }
                    break;
                case "3":
                    System.out.print("Введите домен email (например, @example.com): ");
                    String domain = scanner.nextLine().trim();
                    if (!domain.isEmpty()) {
                        if (!domain.startsWith("@")) domain = "@" + domain;
                        filter = UserFilters.byEmailDomain(domain);
                    }
                    break;
                case "4":
                    System.out.print("Введите подстроку для поиска в полном имени: ");
                    String substr4 = scanner.nextLine().trim();
                    if (!substr4.isEmpty()) {
                        filter = UserFilters.byFullNameContains(substr4);
                    }
                    break;
                default:
                    System.out.println("Неверный выбор.");
                    return;
            }

            if (filter == null) {
                System.out.println("Пустой запрос — поиск отменён.");
                return;
            }

            List<User> results = system.getUserManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("\nНет пользователей, соответствующих фильтру.");
            } else {
                System.out.println("\nНайдено " + results.size() + " пользователь(ей):");
                System.out.printf("%-20s | %-25s | %s%n", "Username", "Full Name", "Email");
                System.out.println("-".repeat(70));
                for (User user : results) {
                    System.out.printf("%-20s | %-25s | %s%n",
                            user.username(),
                            user.fullName(),
                            user.email());
                }
            }
        });
    }

    private static void registerRoleList(CommandParser parser) {
        parser.registerCommand("role-list", "Вывести список всех ролей", (scanner, system) -> {
            System.out.println("\n=== Список ролей ===");
            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("Нет зарегистрированных ролей.");
                return;
            }

            System.out.printf("%-25s | %-10s | %s%n", "Название роли", "Права", "ID");
            System.out.println("-".repeat(65));
            for (Role role : roles) {
                System.out.printf("%-25s | %-10d | %s%n",
                        role.name(),
                        role.getPermissions().size(),
                        role.id());
            }
            System.out.println("=".repeat(65));
        });
    }


}