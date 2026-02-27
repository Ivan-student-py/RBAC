package ru.zvonkov.rbac;

import java.util.Scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandRegistry {
    public static void registerAllCommands(CommandParser parser) {
        // === Служебные команды ===
        registerHelp(parser);
        registerStats(parser);
        registerClear(parser);
        registerExit(parser);

        // === Команды управления пользователями ===
        registerUserList(parser);


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
}