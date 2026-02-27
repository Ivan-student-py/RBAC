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


        // === Команды управления ролями ===

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
}