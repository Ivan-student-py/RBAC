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
        registerAssignmentExtend(parser);
        registerAssignmentSearch(parser);

        // === Команды просмотра прав ===
        registerPermissionsUser(parser);
        registerPermissionsCheck(parser);

        // === Команда просмотра аудита ===
        registerAuditLog(parser);

        // === Команды отчётов ===
        registerReportUsers(parser);
        registerReportRoles(parser);
        registerReportMatrix(parser);
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

            String[] headers = {"Username", "Full Name", "Email"};
            List<String[]> rows = new ArrayList<>();
            for (User user : users) {
                rows.add(new String[]{user.username(), user.fullName(), user.email()});
            }

            String table = FormatUtils.formatTable(headers, rows);
            System.out.println(table);
        });
    }

    private static void registerUserCreate(CommandParser parser) {
        parser.registerCommand("user-create", "Создать нового пользователя", (scanner, system) -> {
            System.out.println("\n=== Создание нового пользователя ===");

            try {
                String username = ConsoleUtils.promptString(scanner,
                        "Введите username (3–20 символов, буквы, цифры, _):", true);

                String fullName = ConsoleUtils.promptString(scanner,
                        "Введите полное имя:", true);

                String email = ConsoleUtils.promptString(scanner,
                        "Введите email:", true);

                User user = User.validate(username, fullName, email);
                system.getUserManager().add(user);

                system.getAuditLog().log("user-create", system.getCurrentUser(), username,
                        "Created user: " + fullName + " <" + email + ">");

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
                String newFullName = ConsoleUtils.promptString(scanner,
                        "Введите новое полное имя (оставьте пустым, чтобы оставить без изменений):", false);
                if (newFullName.isEmpty()) {
                    newFullName = current.fullName();
                }

                String newEmail = ConsoleUtils.promptString(scanner,
                        "Введите новый email (оставьте пустым, чтобы оставить без изменений):", false);
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

            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (!assignments.isEmpty()) {
                System.out.println("\n У пользователя есть " + assignments.size() + " назначений(е/ий).");
                System.out.println("Все назначения будут удалены автоматически.");
            }

            boolean confirmed = ConsoleUtils.promptYesNo(scanner, "\nПодтвердите удаление");
            if (!confirmed) {
                System.out.println("Удаление отменено.");
                return;
            }

            try {
                for (RoleAssignment assignment : assignments) {
                    system.getAssignmentManager().remove(assignment);
                }

                boolean removed = system.getUserManager().remove(user);
                if (removed) {
                    system.getAuditLog().log("user-delete", system.getCurrentUser(), username,
                            "Deleted user and " + assignments.size() + " assignments");
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

            int choice = ConsoleUtils.promptInt(scanner, "Введите номер фильтра", 1, 4);
            UserFilter filter = null;

            switch (choice) {
                case 1:
                    String substr1 = ConsoleUtils.promptString(scanner,
                            "Введите подстроку для поиска в username:", true);
                    filter = UserFilters.byUsernameContains(substr1);
                    break;
                case 2:
                    String substr2 = ConsoleUtils.promptString(scanner,
                            "Введите подстроку для поиска в email:", true);
                    filter = UserFilters.byEmailContains(substr2);
                    break;
                case 3:
                    String domain = ConsoleUtils.promptString(scanner,
                            "Введите домен email (например, @example.com):", true);
                    if (!domain.startsWith("@")) domain = "@" + domain;
                    filter = UserFilters.byEmailDomain(domain);
                    break;
                case 4:
                    String substr4 = ConsoleUtils.promptString(scanner,
                            "Введите подстроку для поиска в полном имени:", true);
                    filter = UserFilters.byFullNameContains(substr4);
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
                if (results.isEmpty()) {
                    System.out.println("\nНет пользователей, соответствующих фильтру.");
                } else {
                    System.out.println("\nНайдено " + results.size() + " пользователь(ей):");

                    String[] headers = {"Username", "Full Name", "Email"};
                    List<String[]> rows = new ArrayList<>();
                    for (User user : results) {
                        rows.add(new String[]{user.username(), user.fullName(), user.email()});
                    }

                    String table = FormatUtils.formatTable(headers, rows);
                    System.out.println(table);
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

            String[] headers = {"Название роли", "Права", "ID"};
            List<String[]> rows = new ArrayList<>();
            for (Role role : roles) {
                rows.add(new String[]{role.name(),
                        String.valueOf(role.getPermissions().size()),
                        role.id()});
            }

            String table = FormatUtils.formatTable(headers, rows);
            System.out.println(table);
        });
    }

    private static void registerRoleCreate(CommandParser parser) {
        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            System.out.println("\n=== Создание новой роли ===");

            try {
                String name = ConsoleUtils.promptString(scanner, "Введите название роли:", true);
                String description = ConsoleUtils.promptString(scanner,
                        "Введите описание роли (оставьте пустым для 'Без описания'):", false);
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

                system.getAuditLog().log("role-create", system.getCurrentUser(), name,
                        "Created role: " + description + " with " + role.getPermissions().size() + " permissions");

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

            boolean confirmed = ConsoleUtils.promptYesNo(scanner, "\nПодтвердите удаление");
            if (!confirmed) {
                System.out.println("Удаление отменено.");
                return;
            }

            try {
                boolean removed = system.getRoleManager().remove(role);
                if (removed) {
                    system.getAuditLog().log("role-delete", system.getCurrentUser(), roleName,
                            "Deleted role with " + assignments.size() + " assignments");

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
                String permName = ConsoleUtils.promptString(scanner,
                        "Введите имя права (например, READ, WRITE):", true);

                String resource = ConsoleUtils.promptString(scanner,
                        "Введите ресурс (например, users, roles):", true);

                String description = ConsoleUtils.promptString(scanner,
                        "Введите описание права (оставьте пустым для 'Без описания'):", false);
                if (description.isEmpty()) {
                    description = "Без описания";
                }

                Permission permission = new Permission(permName, resource, description);
                system.getRoleManager().addPermissionToRole(roleName, permission);

                System.out.println("Право успешно добавлено к роли '" + roleName + "':");

                system.getAuditLog().log("role-add-permission", system.getCurrentUser(), roleName,
                        "Added permission: " + permName + " on " + resource);

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

            try {
                int index = ConsoleUtils.promptInt(scanner,
                        "\nВведите номер права для удаления", 1, permList.size()) - 1;

                Permission permissionToRemove = permList.get(index);
                system.getRoleManager().removePermissionFromRole(roleName, permissionToRemove);

                System.out.println("Право успешно удалено:");

                system.getAuditLog().log("role-remove-permission", system.getCurrentUser(), roleName,
                        "Removed permission: " + permissionToRemove.name() + " on " + permissionToRemove.resource());

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

            int choice = ConsoleUtils.promptInt(scanner, "Введите номер фильтра", 1, 3);
            RoleFilter filter = null;

            switch (choice) {
                case 1:
                    String nameSubstr = ConsoleUtils.promptString(scanner,
                            "Введите подстроку для поиска в названии:", true);
                    filter = RoleFilters.byNameContains(nameSubstr);
                    break;

                case 2:
                    String permName = ConsoleUtils.promptString(scanner,
                            "Введите имя права (например, READ):", true);
                    String resource = ConsoleUtils.promptString(scanner,
                            "Введите ресурс (например, users):", true);
                    filter = RoleFilters.hasPermission(permName, resource);
                    break;

                case 3:
                    int minCount = ConsoleUtils.promptInt(scanner,
                            "Введите минимальное количество прав", 0, Integer.MAX_VALUE);
                    filter = RoleFilters.minPermissions(minCount);
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
                if (results.isEmpty()) {
                    System.out.println("\nНет ролей, соответствующих фильтру.");
                } else {
                    System.out.println("\nНайдено " + results.size() + " роль(ей):");

                    String[] headers = {"Название роли", "Права", "ID"};
                    List<String[]> rows = new ArrayList<>();
                    for (Role role : results) {
                        rows.add(new String[]{
                                role.name(),
                                String.valueOf(role.getPermissions().size()),
                                role.id()
                        });
                    }

                    String table = FormatUtils.formatTable(headers, rows);
                    System.out.println(table);
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

                String reason = ConsoleUtils.promptString(scanner,
                        "Введите причину назначения (оставьте пустым для 'Без указания причины'):", false);
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

                String assignmentType = isTemporary ? "TEMPORARY" : "PERMANENT";
                system.getAuditLog().log("assign-role", system.getCurrentUser(), username,
                        "Assigned role '" + selectedRole.name() + "' (" + assignmentType + ") with reason: " + reason);

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

            try {
                int index = ConsoleUtils.promptInt(scanner,
                        "\nВыберите номер назначения для отзыва", 1, activeAssignments.size()) - 1;

                RoleAssignment assignment = activeAssignments.get(index);
                boolean success = system.getAssignmentManager().remove(assignment);

                if (success) {
                    system.getAuditLog().log("revoke-role", system.getCurrentUser(), username,
                            "Revoked role '" + assignment.role().name() + "' (assignment ID: " + assignment.assignmentId() + ")");

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

            String[] headers = {"Username", "Роль", "Тип", "Статус", "Назначено"};
            List<String[]> rows = new ArrayList<>();
            for (RoleAssignment a : assignments) {
                String type = (a instanceof PermanentAssignment) ? "Постоянное" : "Временное";
                String status = a.isActive() ? "Активно" : "Неактивно";
                rows.add(new String[]{a.user().username(),
                        a.role().name(),
                        type,
                        status,
                        a.metadata().assignedAt()});
            }

            String table = FormatUtils.formatTable(headers, rows);
            System.out.println(table);
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

            String[] headers = {"#", "Роль", "Тип", "Статус", "Назначено", "Причина", "Истекает"};
            List<String[]> rows = new ArrayList<>();
            for (int i = 0; i < assignments.size(); i++) {
                RoleAssignment a = assignments.get(i);
                String type = (a instanceof PermanentAssignment) ? "Постоянное" : "Временное";
                String status = a.isActive() ? "Активно" : "Неактивно";
                String expires = (a instanceof TemporaryAssignment)
                        ? ((TemporaryAssignment) a).expiresAt()
                        : "-";

                rows.add(new String[]{
                        String.valueOf(i + 1),
                        a.role().name(),
                        type,
                        status,
                        a.metadata().assignedAt(),
                        a.metadata().reason() != null ? a.metadata().reason() : "-",
                        expires
                });
            }

            System.out.println(FormatUtils.formatHeader("Назначения пользователя '" + username + "'"));
            String table = FormatUtils.formatTable(headers, rows);
            System.out.println(table);
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

            String[] headers = {"#", "Username", "Статус", "Тип", "Назначено", "Истекает"};
            List<String[]> rows = new ArrayList<>();
            for (int i = 0; i < assignments.size(); i++) {
                RoleAssignment a = assignments.get(i);
                String type = (a instanceof PermanentAssignment) ? "Постоянное" : "Временное";
                String status = a.isActive() ? "Активно" : "Неактивно";
                String expires = (a instanceof TemporaryAssignment)
                        ? ((TemporaryAssignment) a).expiresAt()
                        : "-";

                rows.add(new String[]{
                        String.valueOf(i + 1),
                        a.user().username(),
                        status,
                        type,
                        a.metadata().assignedAt(),
                        expires
                });
            }

            System.out.println(FormatUtils.formatHeader("Пользователи с ролью '" + roleName + "'"));
            String table = FormatUtils.formatTable(headers, rows);
            System.out.println(table);
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

            String[] headers = {"Username", "Роль", "Тип", "Назначено"};
            List<String[]> rows = new ArrayList<>();
            for (RoleAssignment a : activeAssignments) {
                String type = (a instanceof PermanentAssignment) ? "Постоянное" : "Временное";
                rows.add(new String[]{
                        a.user().username(),
                        a.role().name(),
                        type,
                        a.metadata().assignedAt()
                });
            }

            System.out.println(FormatUtils.formatHeader("Активные назначения"));
            String table = FormatUtils.formatTable(headers, rows);
            System.out.println(table);
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

            String[] headers = {"Username", "Роль", "Тип", "Назначено", "Истекло"};
            List<String[]> rows = new ArrayList<>();
            for (RoleAssignment a : expiredAssignments) {
                if (!(a instanceof TemporaryAssignment)) continue;

                TemporaryAssignment temp = (TemporaryAssignment) a;
                rows.add(new String[]{
                        a.user().username(),
                        a.role().name(),
                        "Временное",
                        a.metadata().assignedAt(),
                        temp.expiresAt()
                });
            }

            System.out.println(FormatUtils.formatHeader("Истёкшие временные назначения"));
            String table = FormatUtils.formatTable(headers, rows);
            System.out.println(table);
        });
    }

    private static void registerAssignmentExtend(CommandParser parser) {
        parser.registerCommand("assignment-extend", "Продлить временное назначение", (scanner, system) -> {
            System.out.print("\nВведите username пользователя: ");
            String username = scanner.nextLine().trim();
            if (username.isEmpty()) {
                System.out.println("Username не может быть пустым.");
                return;
            }

            System.out.print("Введите название роли: ");
            String roleName = scanner.nextLine().trim();
            if (roleName.isEmpty()) {
                System.out.println("Название роли не может быть пустым.");
                return;
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь '" + username + "' не найден.");
                return;
            }
            User user = userOpt.get();

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("Роль '" + roleName + "' не найдена.");
                return;
            }
            Role role = roleOpt.get();

            List<RoleAssignment> assignments = system.getAssignmentManager()
                    .getActiveAssignments().stream()
                    .filter(a -> a.user().username().equals(username) &&
                            a.role().name().equals(roleName))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            TemporaryAssignment target = null;
            for (RoleAssignment a : assignments) {
                if (a instanceof TemporaryAssignment) {
                    target = (TemporaryAssignment) a;
                    break;
                }
            }

            if (target == null) {
                System.out.println("Активное временное назначение роли '" + roleName +
                        "' пользователю '" + username + "' не найдено.");
                return;
            }

            System.out.println("Текущая дата истечения: " + target.expiresAt());
            String newExpiresAt = ConsoleUtils.promptString(scanner,
                    "Введите новую дату истечения (формат: yyyy-MM-dd HH:mm):", true);

            try {
                system.getAssignmentManager().extendTemporaryAssignment(target.assignmentId(), newExpiresAt);

                system.getAuditLog().log("assignment-extend", system.getCurrentUser(), username,
                        "Extended role '" + roleName + "' until " + newExpiresAt);

                System.out.println("Назначение успешно продлено до: " + newExpiresAt);
            } catch (Exception e) {
                System.out.println("Ошибка при продлении: " + e.getMessage());
            }
        });
    }

    private static void registerAssignmentSearch(CommandParser parser) {
        parser.registerCommand("assignment-search", "Поиск назначений по фильтрам", (scanner, system) -> {
            System.out.println("\n=== Поиск назначений ===");
            System.out.println("Выберите тип фильтра:");
            System.out.println("1. По пользователю (username)");
            System.out.println("2. По роли");
            System.out.println("3. По типу (постоянное/временное)");
            System.out.println("4. По статусу (активное/неактивное)");
            System.out.println("5. Назначенные после даты");
            System.out.println("6. Истекающие до даты");

            int choice = ConsoleUtils.promptInt(scanner, "Введите номер фильтра", 1, 6);
            AssignmentFilter filter = null;

            switch (choice) {
                case 1:
                    String user = ConsoleUtils.promptString(scanner, "Введите username:", true);
                    filter = a -> a.user().username().equals(user);
                    break;
                case 2:
                    String role = ConsoleUtils.promptString(scanner, "Введите название роли:", true);
                    filter = a -> a.role().name().equals(role);
                    break;
                case 3:
                    int typeChoice = ConsoleUtils.promptInt(scanner, "Тип: 1 — постоянное, 2 — временное", 1, 2);
                    boolean isTemp = typeChoice == 2;
                    filter = a -> (a instanceof TemporaryAssignment) == isTemp;
                    break;
                case 4:
                    int statusChoice = ConsoleUtils.promptInt(scanner, "Статус: 1 — активное, 2 — неактивное", 1, 2);
                    boolean active = statusChoice == 1;
                    filter = AssignmentFilters.byStatus(active);
                    break;
                case 5:
                    String afterDate = ConsoleUtils.promptString(scanner,
                            "Дата (формат: yyyy-MM-dd HH:mm):", true);
                    filter = a -> a.metadata().assignedAt().compareTo(afterDate) > 0;
                    break;
                case 6:
                    String beforeDate = ConsoleUtils.promptString(scanner,
                            "Дата (формат: yyyy-MM-dd HH:mm):", true);
                    filter = a -> (a instanceof TemporaryAssignment) &&
                            ((TemporaryAssignment) a).expiresAt().compareTo(beforeDate) < 0;
                    break;
                default:
                    System.out.println("Неверный выбор.");
                    return;
            }

            if (filter == null) {
                System.out.println("Пустой запрос — поиск отменён.");
                return;
            }

            List<RoleAssignment> results = system.getAssignmentManager().findByFilter(filter);
            if (results.isEmpty()) {
                System.out.println("\nНет назначений, соответствующих фильтру.");
            } else {
                if (results.isEmpty()) {
                    System.out.println("\nНет назначений, соответствующих фильтру.");
                } else {
                    System.out.println("\nНайдено " + results.size() + " назначение(ий):");

                    String[] headers = {"Username", "Роль", "Тип", "Статус", "Назначено"};
                    List<String[]> rows = new ArrayList<>();
                    for (RoleAssignment a : results) {
                        String type = (a instanceof PermanentAssignment) ? "Постоянное" : "Временное";
                        String status = a.isActive() ? "Активно" : "Неактивно";
                        rows.add(new String[]{
                                a.user().username(),
                                a.role().name(),
                                type,
                                status,
                                a.metadata().assignedAt()
                        });
                    }

                    String table = FormatUtils.formatTable(headers, rows);
                    System.out.println(table);
                }
            }
        });
    }

    private static void registerPermissionsUser(CommandParser parser) {
        parser.registerCommand("permissions-user", "Все права конкретного пользователя", (scanner, system) -> {
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

            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);
            if (permissions.isEmpty()) {
                System.out.println("У пользователя '" + username + "' нет прав доступа.");
                return;
            }

            System.out.println("\n=== Права пользователя '" + username + "' ===");
            Map<String, List<Permission>> byResource = new HashMap<>();
            for (Permission p : permissions) {
                byResource.computeIfAbsent(p.resource(), k -> new ArrayList<>()).add(p);
            }

            if (permissions.isEmpty()) {
                System.out.println("У пользователя '" + username + "' нет прав доступа.");
                return;
            }

            System.out.println(FormatUtils.formatHeader("Права пользователя '" + username + "'"));

            byResource.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        System.out.println("\n" + FormatUtils.formatBox("Ресурс: " + entry.getKey()));
                        for (Permission p : entry.getValue()) {
                            System.out.println("  . " + p.name() + " — " + p.description());
                        }
                    });
        });
    }

    private static void registerPermissionsCheck(CommandParser parser) {
        parser.registerCommand("permissions-check", "Проверить наличие конкретного права у пользователя", (scanner, system) -> {
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

            String permName = ConsoleUtils.promptString(scanner,
                    "Введите имя права (например, READ):", true);

            String resource = ConsoleUtils.promptString(scanner,
                    "Введите ресурс (например, users):", true);

            boolean hasPermission = system.getAssignmentManager().userHasPermission(user, permName, resource);
            if (!hasPermission) {
                System.out.println("\nУ пользователя '" + username + "' нет права '" + permName + "' на ресурс '" + resource + "'.");
                return;
            }

            List<String> sourceRoles = new ArrayList<>();
            List<RoleAssignment> activeAssignments = system.getAssignmentManager().getActiveAssignments();
            for (RoleAssignment a : activeAssignments) {
                if (!a.user().username().equals(username)) continue;
                if (a.role().hasPermission(permName, resource)) {
                    sourceRoles.add(a.role().name());
                }
            }

            System.out.println("\nПраво найдено!");
            System.out.println("Пользователь: " + username);
            System.out.println("Право: " + permName + " на ресурс: " + resource);
            if (!sourceRoles.isEmpty()) {
                System.out.println("Предоставлено ролями: " + String.join(", ", sourceRoles));
            } else {
                System.out.println("Право обнаружено, но источник не определён.");
            }
        });
    }

    private static void registerAuditLog(CommandParser parser) {
        parser.registerCommand("audit-log", "Просмотреть лог аудита всех действий", (scanner, system) -> {
            System.out.println("\n=== Лог аудита ===");
            AuditLog auditLog = system.getAuditLog();
            if (auditLog.getAll().isEmpty()) {
                System.out.println("Лог аудита пуст.");
            } else {
                auditLog.printLog();
            }
            System.out.println("=".repeat(50));
        });
    }

    private static void registerReportUsers(CommandParser parser) {
        parser.registerCommand("report-users", "Сгенерировать отчёт по пользователям", (scanner, system) -> {
            System.out.println("\n=== Отчёт по пользователям ===");
            ReportGenerator generator = new ReportGenerator();
            String report = generator.generateUserReport(
                    system.getUserManager(),
                    system.getAssignmentManager()
            );
            System.out.println(report);

            boolean saveToFile = ConsoleUtils.promptYesNo(scanner, "\nСохранить отчёт в файл?");
            if (saveToFile) {
                String filename = ConsoleUtils.promptString(scanner, "Введите имя файла:", true);
                if (!filename.isEmpty()) {
                    try {
                        generator.exportToFile(report, filename);
                        System.out.println("Отчёт сохранён в файл: " + filename);
                    } catch (Exception e) {
                        System.out.println("Ошибка сохранения: " + e.getMessage());
                    }
                }
            }
            System.out.println("=".repeat(50));
        });
    }

    private static void registerReportRoles(CommandParser parser) {
        parser.registerCommand("report-roles", "Сгенерировать отчёт по ролям", (scanner, system) -> {
            System.out.println("\n=== Отчёт по ролям ===");
            ReportGenerator generator = new ReportGenerator();
            String report = generator.generateRoleReport(
                    system.getRoleManager(),
                    system.getAssignmentManager()
            );
            System.out.println(report);

            boolean saveToFile = ConsoleUtils.promptYesNo(scanner, "\nСохранить отчёт в файл?");
            if (saveToFile) {
                String filename = ConsoleUtils.promptString(scanner, "Введите имя файла:", true);
                if (!filename.isEmpty()) {
                    try {
                        generator.exportToFile(report, filename);
                        System.out.println("Отчёт сохранён в файл: " + filename);
                    } catch (Exception e) {
                        System.out.println("Ошибка сохранения: " + e.getMessage());
                    }
                }
            }
            System.out.println("=".repeat(50));
        });
    }

    private static void registerReportMatrix(CommandParser parser) {
        parser.registerCommand("report-matrix", "Сгенерировать матрицу прав", (scanner, system) -> {
            System.out.println("\n=== Матрица прав доступа ===");
            ReportGenerator generator = new ReportGenerator();
            String report = generator.generatePermissionMatrix(
                    system.getUserManager(),
                    system.getAssignmentManager()
            );
            System.out.println(report);

            boolean saveToFile = ConsoleUtils.promptYesNo(scanner, "\nСохранить отчёт в файл?");
            if (saveToFile) {
                String filename = ConsoleUtils.promptString(scanner, "Введите имя файла:", true);
                if (!filename.isEmpty()) {
                    try {
                        generator.exportToFile(report, filename);
                        System.out.println("Отчёт сохранён в файл: " + filename);
                    } catch (Exception e) {
                        System.out.println("Ошибка сохранения: " + e.getMessage());
                    }
                }
            }
            System.out.println("=".repeat(50));
        });
    }
}