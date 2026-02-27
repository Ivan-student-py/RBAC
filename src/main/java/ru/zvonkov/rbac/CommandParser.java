package ru.zvonkov.rbac;

import java.util.*;

public class CommandParser {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> commandDescriptions = new HashMap<>();

    public void registerCommand(String name, String description, Command command) {
        commands.put(name, command);
        commandDescriptions.put(name, description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command command = commands.get(commandName);
        if (command != null) {
            command.execute(scanner, system);
        } else {
            System.out.println("Неизвестная команда: " + commandName);
            printHelp();
        }
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }
        String commandName = input.trim().split("\\s+")[0];
        executeCommand(commandName, scanner, system);
    }

    public void printHelp() {
        System.out.println("\n=== Справка по командам ===");
        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            System.out.printf("%-25s - %s%n", entry.getKey(), entry.getValue());
        }
        System.out.println("===========================\n");
    }
}