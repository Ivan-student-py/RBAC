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
        registerRoleCreate(parser);
        registerRoleView(parser);
        registerRoleUpdate(parser);
        registerRoleDelete(parser);
        registerRoleAddPermission(parser);
        registerRoleRemovePermission(parser);
        registerRoleSearch(parser);

        // === Команды управления назначениями ===
        registerAssignRole(parser);
        registerRevokeRole(parser);
        registerAssignmentList(parser);
        registerAssignmentListUser(parser);
        registerAssignmentListRole(parser);
        registerAssignmentActive(parser);
        registerAssignmentExpired(parser);

        // === Команды просмотра прав ===
    }

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

    private static void registerRoleCreate(CommandParser parser) {
        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            System.out.println("\n=== Создание новой роли ===");

            try {
                System.out.print("Введите название роли: ");
                String name = scanner.nextLine().trim();
                if (name.isEmpty()) {
                    System.out.println("Название роли не может быть пустым.");
                    return;
                }

                System.out.print("Введите описание роли: ");
                String description = scanner.nextLine().trim();
                if (description.isEmpty()) {
                    description = "Без описания";
                }

                Role role = new Role(name, description);
                System.out.println("Роль '" + name + "' создана.");

                while (true) {
                    System.out.print("\nХотите добавить право? (да/нет): ");
                    String addPerm = scanner.nextLine().trim().toLowerCase();
                    if (!"да".equals(addPerm)) break;

                    System.out.print("  Имя права (например, READ, WRITE): ");
                    String permName = scanner.nextLine().trim();
                    if (permName.isEmpty()) {
                        System.out.println("    Имя права не может быть пустым.");
                        continue;
                    }

                    System.out.print("  Ресурс (например, users, roles): ");
                    String resource = scanner.nextLine().trim();
                    if (resource.isEmpty()) {
                        System.out.println("    Ресурс не может быть пустым.");
                        continue;
                    }

                    System.out.print("  Описание права: ");
                    String permDesc = scanner.nextLine().trim();
                    if (permDesc.isEmpty()) {
                        permDesc = "Без описания";
                    }

                    Permission permission = new Permission(permName, resource, permDesc);
                    role.addPermission(permission);
                    System.out.println("    Право добавлено: " + permission.format());
                }

                system.getRoleManager().add(role);
                System.out.println("\nРоль успешно сохранена в системе.");
                System.out.println(role.format());

            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при создании роли: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage());
            }
        });
    }

    private static void registerRoleView(CommandParser parser) {
        parser.registerCommand("role-view", "Просмотр информации о роли", (scanner, system) -> {
            System.out.print("\nВведите название роли: ");
            String roleName = scanner.nextLine().trim();
            if (roleName.isEmpty()) {
                System.out.println("Название роли не может быть пустым.");
                return;
            }

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль с названием '" + roleName + "' не найдена.");
                return;
            }

            Role role = roleOpt.get();
            System.out.println("\n=== Информация о роли ===");
            System.out.println(role.format());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (assignments.isEmpty()) {
                System.out.println("\nЭта роль не назначена ни одному пользователю.");
            } else {
                System.out.println("\nНазначена " + assignments.size() + " пользователю(ям):");
                for (RoleAssignment a : assignments) {
                    System.out.println("  • " + a.user().username() +
                            " (" + (a.isActive() ? "активно" : "неактивно") + ")");
                }
            }
            System.out.println("=".repeat(40));
        });
    }

    private static void registerRoleUpdate(CommandParser parser) {
        parser.registerCommand("role-update", "Обновить название или описание роли", (scanner, system) -> {
            System.out.print("\nВведите текущее название роли для обновления: ");
            String oldName = scanner.nextLine().trim();
            if (oldName.isEmpty()) {
                System.out.println("Название роли не может быть пустым.");
                return;
            }

            Optional<Role> oldRoleOpt = system.getRoleManager().findByName(oldName);
            if (oldRoleOpt.isEmpty()) {
                System.out.println("Роль с названием '" + oldName + "' не найдена.");
                return;
            }

            Role oldRole = oldRoleOpt.get();
            System.out.println("Текущие данные:");
            System.out.println("  Название: " + oldRole.name());
            System.out.println("  Описание: " + oldRole.description());

            try {
                System.out.print("Введите новое название (оставьте пустым, чтобы оставить без изменений): ");
                String newName = scanner.nextLine().trim();
                if (newName.isEmpty()) {
                    newName = oldRole.name();
                }

                System.out.print("Введите новое описание (оставьте пустым, чтобы оставить без изменений): ");
                String newDesc = scanner.nextLine().trim();
                if (newDesc.isEmpty()) {
                    newDesc = oldRole.description();
                }

                if (!newName.equals(oldRole.name()) && system.getRoleManager().exists(newName)) {
                    System.out.println("Роль с названием '" + newName + "' уже существует.");
                    return;
                }

                Role newRole = new Role(newName, newDesc, oldRole.id(), new ArrayList<>(oldRole.getPermissions()));

                boolean removed = system.getRoleManager().remove(oldRole);
                if (!removed) {
                    System.out.println("Не удалось удалить старую роль.");
                    return;
                }

                system.getRoleManager().add(newRole);
                System.out.println("Роль успешно обновлена:");
                System.out.println(newRole.format());

            } catch (Exception e) {
                System.out.println("Ошибка при обновлении роли: " + e.getMessage());
            }
        });
    }

    private static void registerRoleDelete(CommandParser parser) {
        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            System.out.print("\nВведите название роли для удаления: ");
            String roleName = scanner.nextLine().trim();
            if (roleName.isEmpty()) {
                System.out.println("Название роли не может быть пустым.");
                return;
            }

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль с названием '" + roleName + "' не найдена.");
                return;
            }

            Role role = roleOpt.get();
            System.out.println("Вы собираетесь удалить роль:");
            System.out.println("  Название: " + role.name());
            System.out.println("  Описание: " + role.description());

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (!assignments.isEmpty()) {
                System.out.println("\nЭта роль назначена " + assignments.size() + " пользователю(ям):");
                for (RoleAssignment a : assignments) {
                    System.out.println("  • " + a.user().username() +
                            " (" + (a.isActive() ? "активно" : "неактивно") + ")");
                }
                System.out.println("\nУдаление роли приведёт к потере этих назначений!");
            }

            System.out.print("\nПодтвердите удаление (введите \"да\"): ");
            String confirm = scanner.nextLine().trim();
            if (!"да".equals(confirm)) {
                System.out.println("Удаление отменено.");
                return;
            }

            try {
                boolean removed = system.getRoleManager().remove(role);
                if (removed) {
                    System.out.println("Роль успешно удалена.");
                } else {
                    System.out.println("Не удалось удалить роль.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка при удалении: " + e.getMessage());
            }
        });
    }

    private static void registerRoleAddPermission(CommandParser parser) {
        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, system) -> {
            System.out.print("\nВведите название роли: ");
            String roleName = scanner.nextLine().trim();
            if (roleName.isEmpty()) {
                System.out.println("Название роли не может быть пустым.");
                return;
            }

            if (!system.getRoleManager().exists(roleName)) {
                System.out.println("Роль с названием '" + roleName + "' не найдена.");
                return;
            }

            try {
                System.out.print("Введите имя права (например, READ, WRITE): ");
                String permName = scanner.nextLine().trim();
                if (permName.isEmpty()) {
                    System.out.println("Имя права не может быть пустым.");
                    return;
                }

                System.out.print("Введите ресурс (например, users, roles): ");
                String resource = scanner.nextLine().trim();
                if (resource.isEmpty()) {
                    System.out.println("Ресурс не может быть пустым.");
                    return;
                }

                System.out.print("Введите описание права: ");
                String description = scanner.nextLine().trim();
                if (description.isEmpty()) {
                    description = "Без описания";
                }

                Permission permission = new Permission(permName, resource, description);
                system.getRoleManager().addPermissionToRole(roleName, permission);

                System.out.println("Право успешно добавлено к роли '" + roleName + "':");
                System.out.println("  " + permission.format());

            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при добавлении права: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage());
            }
        });
    }

    private static void registerRoleRemovePermission(CommandParser parser) {
        parser.registerCommand("role-remove-permission", "Удалить право из роли", (scanner, system) -> {
            System.out.print("\nВведите название роли: ");
            String roleName = scanner.nextLine().trim();
            if (roleName.isEmpty()) {
                System.out.println("Название роли не может быть пустым.");
                return;
            }

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль с названием '" + roleName + "' не найдена.");
                return;
            }

            Role role = roleOpt.get();
            Set<Permission> permissions = role.getPermissions();
            if (permissions.isEmpty()) {
                System.out.println("У роли '" + roleName + "' нет прав для удаления.");
                return;
            }

            List<Permission> permList = new ArrayList<>(permissions);
            System.out.println("\nПрава роли '" + roleName + "':");
            for (int i = 0; i < permList.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, permList.get(i).format());
            }

            System.out.print("\nВведите номер права для удаления (1–" + permList.size() + "): ");
            String input = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(input) - 1;
                if (index < 0 || index >= permList.size()) {
                    System.out.println("Неверный номер.");
                    return;
                }

                Permission permissionToRemove = permList.get(index);
                system.getRoleManager().removePermissionFromRole(roleName, permissionToRemove);

                System.out.println("Право успешно удалено:");
                System.out.println("  " + permissionToRemove.format());

            } catch (NumberFormatException e) {
                System.out.println("Введите корректный номер.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при удалении права: " + e.getMessage());
            }
        });
    }

    private static void registerRoleSearch(CommandParser parser) {
        parser.registerCommand("role-search", "Поиск ролей по фильтрам", (scanner, system) -> {
            System.out.println("\n=== Поиск ролей ===");
            System.out.println("Выберите тип фильтра:");
            System.out.println("1. По названию (содержит)");
            System.out.println("2. По наличию конкретного права");
            System.out.println("3. По минимальному количеству прав");
            System.out.print("Введите номер фильтра (1–3): ");

            String choice = scanner.nextLine().trim();
            RoleFilter filter = null;

            switch (choice) {
                case "1":
                    System.out.print("Введите подстроку для поиска в названии: ");
                    String nameSubstr = scanner.nextLine().trim();
                    if (!nameSubstr.isEmpty()) {
                        filter = RoleFilters.byNameContains(nameSubstr);
                    }
                    break;

                case "2":
                    System.out.print("Введите имя права (например, READ): ");
                    String permName = scanner.nextLine().trim();
                    System.out.print("Введите ресурс (например, users): ");
                    String resource = scanner.nextLine().trim();
                    if (!permName.isEmpty() && !resource.isEmpty()) {
                        filter = RoleFilters.hasPermission(permName, resource);
                    } else {
                        System.out.println("Оба поля должны быть заполнены.");
                        return;
                    }
                    break;

                case "3":
                    System.out.print("Введите минимальное количество прав: ");
                    String countStr = scanner.nextLine().trim();
                    try {
                        int minCount = Integer.parseInt(countStr);
                        if (minCount >= 0) {
                            filter = RoleFilters.minPermissions(minCount);
                        } else {
                            System.out.println("Минимальное количество не может быть отрицательным.");
                            return;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Введите корректное число.");
                        return;
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

            List<Role> results = system.getRoleManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("\nНет ролей, соответствующих фильтру.");
            } else {
                System.out.println("\nНайдено " + results.size() + " роль(ей):");
                System.out.printf("%-25s | %-10s | %s%n", "Название роли", "Права", "ID");
                System.out.println("-".repeat(65));
                for (Role role : results) {
                    System.out.printf("%-25s | %-10d | %s%n",
                            role.name(),
                            role.getPermissions().size(),
                            role.id());
                }
            }
        });
    }

    private static void registerAssignRole(CommandParser parser) {
        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {
            System.out.print("\nВведите username пользователя: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username не может быть пустым.");
                return;
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь '" + username + "' не найден.");
                return;
            }
            User user = userOpt.get();

            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("Нет доступных ролей для назначения.");
                return;
            }

            System.out.println("\nДоступные роли:");
            for (int i = 0; i < roles.size(); i++) {
                System.out.printf("  %d. %s (%d прав)\n", i + 1, roles.get(i).name(), roles.get(i).getPermissions().size());
            }

            System.out.print("\nВыберите номер роли (1–" + roles.size() + "): ");
            String roleInput = scanner.nextLine().trim();
            try {
                int roleIndex = Integer.parseInt(roleInput) - 1;
                if (roleIndex < 0 || roleIndex >= roles.size()) {
                    System.out.println("Неверный номер роли.");
                    return;
                }
                Role selectedRole = roles.get(roleIndex);

                System.out.println("\nТип назначения:");
                System.out.println("1. Постоянное");
                System.out.println("2. Временное");
                System.out.print("Выберите тип (1 или 2): ");
                String typeInput = scanner.nextLine().trim();
                boolean isTemporary = "2".equals(typeInput);

                String expiresAt = null;
                if (isTemporary) {
                    System.out.print("Введите дату истечения (формат: yyyy-MM-dd HH:mm): ");
                    expiresAt = scanner.nextLine().trim();
                    if (expiresAt.isEmpty()) {
                        System.out.println("Дата истечения обязательна для временного назначения.");
                        return;
                    }
                }

                System.out.print("Введите причину назначения: ");
                String reason = scanner.nextLine().trim();
                if (reason.isEmpty()) {
                    reason = "Без указания причины";
                }

                AssignmentMetadata meta = AssignmentMetadata.now(system.getCurrentUser(), reason);
                RoleAssignment assignment;
                if (isTemporary) {
                    assignment = new TemporaryAssignment(user, selectedRole, meta, expiresAt, false);
                } else {
                    assignment = new PermanentAssignment(user, selectedRole, meta);
                }

                system.getAssignmentManager().add(assignment);
                System.out.println("Роль '" + selectedRole.name() + "' успешно назначена пользователю '" + username + "'.");

            } catch (NumberFormatException e) {
                System.out.println("Введите корректный номер.");
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка при назначении: " + e.getMessage());
            }
        });
    }

    private static void registerRevokeRole(CommandParser parser) {
        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, system) -> {
            System.out.print("\nВведите username пользователя: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username не может быть пустым.");
                return;
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь '" + username + "' не найден.");
                return;
            }
            User user = userOpt.get();

            List<RoleAssignment> activeAssignments = system.getAssignmentManager()
                    .getActiveAssignments().stream()
                    .filter(a -> a.user().username().equals(username))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            if (activeAssignments.isEmpty()) {
                System.out.println("У пользователя '" + username + "' нет активных назначений.");
                return;
            }

            System.out.println("\nАктивные назначения пользователя '" + username + "':");
            for (int i = 0; i < activeAssignments.size(); i++) {
                RoleAssignment a = activeAssignments.get(i);
                String type = (a instanceof PermanentAssignment) ? "Постоянное" : "Временное";
                System.out.printf("  %d. Роль: %s | Тип: %s | Назначено: %s\n",
                        i + 1,
                        a.role().name(),
                        type,
                        a.metadata().assignedAt());
            }

            System.out.print("\nВыберите номер назначения для отзыва (1–" + activeAssignments.size() + "): ");
            String input = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(input) - 1;
                if (index < 0 || index >= activeAssignments.size()) {
                    System.out.println("Неверный номер.");
                    return;
                }

                RoleAssignment assignment = activeAssignments.get(index);
                boolean success = system.getAssignmentManager().remove(assignment);

                if (success) {
                    System.out.println("Назначение успешно отозвано.");
                } else {
                    System.out.println("Не удалось отозвать назначение.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Введите корректный номер.");
            }
        });
    }

    private static void registerAssignmentList(CommandParser parser) {
        parser.registerCommand("assignment-list", "Вывести список всех назначений", (scanner, system) -> {
            System.out.println("\n=== Список всех назначений ===");
            List<RoleAssignment> assignments = system.getAssignmentManager().findAll();
            if (assignments.isEmpty()) {
                System.out.println("Нет зарегистрированных назначений.");
                return;
            }

            System.out.printf("%-15s | %-20s | %-12s | %-10s | %s%n",
                    "Username", "Роль", "Тип", "Статус", "Назначено");
            System.out.println("-".repeat(85));

            for (RoleAssignment a : assignments) {
                String username = a.user().username();
                String roleName = a.role().name();
                String type = (a instanceof PermanentAssignment) ? "Постоянное" : "Временное";
                String status = a.isActive() ? "Активно" : "Неактивно";
                String assignedAt = a.metadata().assignedAt();

                System.out.printf("%-15s | %-20s | %-12s | %-10s | %s%n",
                        username, roleName, type, status, assignedAt);
            }
            System.out.println("=".repeat(85));
        });
    }

    private static void registerAssignmentListUser(CommandParser parser) {
        parser.registerCommand("assignment-list-user", "Назначения конкретного пользователя", (scanner, system) -> {
            System.out.print("\nВведите username пользователя: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username не может быть пустым.");
                return;
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь '" + username + "' не найден.");
                return;
            }
            User user = userOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("У пользователя '" + username + "' нет назначений.");
                return;
            }

            System.out.println("\n=== Назначения пользователя '" + username + "' ===");
            for (int i = 0; i < assignments.size(); i++) {
                RoleAssignment a = assignments.get(i);
                System.out.printf("\n%d. Роль: %s\n", i + 1, a.role().name());
                System.out.println("   Тип: " + (a instanceof PermanentAssignment ? "Постоянное" : "Временное"));
                System.out.println("   Статус: " + (a.isActive() ? "Активно" : "Неактивно"));
                System.out.println("   Назначено: " + a.metadata().assignedAt());
                System.out.println("   Причина: " + a.metadata().reason());

                if (a instanceof TemporaryAssignment) {
                    System.out.println("   Истекает: " + ((TemporaryAssignment) a).expiresAt());
                }
            }
            System.out.println("=".repeat(50));
        });
    }

    private static void registerAssignmentListRole(CommandParser parser) {
        parser.registerCommand("assignment-list-role", "Пользователи с конкретной ролью", (scanner, system) -> {
            System.out.print("\nВведите название роли: ");
            String roleName = scanner.nextLine().trim();
            if (roleName.isEmpty()) {
                System.out.println("Название роли не может быть пустым.");
                return;
            }

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль '" + roleName + "' не найдена.");
                return;
            }
            Role role = roleOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (assignments.isEmpty()) {
                System.out.println("Роль '" + roleName + "' не назначена ни одному пользователю.");
                return;
            }

            System.out.println("\n=== Пользователи с ролью '" + roleName + "' ===");
            for (int i = 0; i < assignments.size(); i++) {
                RoleAssignment a = assignments.get(i);
                System.out.printf("\n%d. Username: %s\n", i + 1, a.user().username());
                System.out.println("   Статус: " + (a.isActive() ? "Активно" : "Неактивно"));
                System.out.println("   Тип: " + (a instanceof PermanentAssignment ? "Постоянное" : "Временное"));
                System.out.println("   Назначено: " + a.metadata().assignedAt());

                if (a instanceof TemporaryAssignment) {
                    System.out.println("   Истекает: " + ((TemporaryAssignment) a).expiresAt());
                }
            }
            System.out.println("=".repeat(50));
        });
    }

    private static void registerAssignmentActive(CommandParser parser) {
        parser.registerCommand("assignment-active", "Только активные назначения", (scanner, system) -> {
            System.out.println("\n=== Активные назначения ===");
            List<RoleAssignment> activeAssignments = system.getAssignmentManager().getActiveAssignments();
            if (activeAssignments.isEmpty()) {
                System.out.println("Нет активных назначений.");
                return;
            }

            System.out.printf("%-15s | %-20s | %-12s | %s%n",
                    "Username", "Роль", "Тип", "Назначено");
            System.out.println("-".repeat(70));

            for (RoleAssignment a : activeAssignments) {
                String username = a.user().username();
                String roleName = a.role().name();
                String type = (a instanceof PermanentAssignment) ? "Постоянное" : "Временное";
                String assignedAt = a.metadata().assignedAt();

                System.out.printf("%-15s | %-20s | %-12s | %s%n",
                        username, roleName, type, assignedAt);
            }
            System.out.println("=".repeat(70));
        });
    }

    private static void registerAssignmentExpired(CommandParser parser) {
        parser.registerCommand("assignment-expired", "Истёкшие временные назначения", (scanner, system) -> {
            System.out.println("\n=== Истёкшие временные назначения ===");
            List<RoleAssignment> expiredAssignments = system.getAssignmentManager().getExpiredAssignments();
            if (expiredAssignments.isEmpty()) {
                System.out.println("Нет истёкших временных назначений.");
                return;
            }

            System.out.printf("%-15s | %-20s | %-12s | %-20s | %s%n",
                    "Username", "Роль", "Тип", "Назначено", "Истекло");
            System.out.println("-".repeat(90));

            for (RoleAssignment a : expiredAssignments) {
                if (!(a instanceof TemporaryAssignment)) continue;

                String username = a.user().username();
                String roleName = a.role().name();
                String assignedAt = a.metadata().assignedAt();
                String expiredAt = ((TemporaryAssignment) a).expiresAt();

                System.out.printf("%-15s | %-20s | %-12s | %-20s | %s%n",
                        username, roleName, "Временное", assignedAt, expiredAt);
            }
            System.out.println("=".repeat(90));
        });
    }


}