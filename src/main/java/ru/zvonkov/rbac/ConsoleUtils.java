package ru.zvonkov.rbac;

import java.util.List;
import java.util.Scanner;

public final class ConsoleUtils {

    private ConsoleUtils() {}

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message + (required ? " " : " (or press Enter to skip) "));

            String input = scanner.nextLine().trim();

            if (!required && input.isEmpty()) {
                return "";
            }

            if (input.isEmpty()) {
                System.out.println("This field is required. Try again.");
                continue;
            }

            return input;
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message + " [" + min + "-" + max + "] ");

            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);

                if (value < min || value > max) {
                    System.out.println("The number must be between " + min + " and " + max);
                    continue;
                }

                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Try again.");
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (yes/no) ");

            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("да") || input.equals("yes") || input.equals("y")) {
                return true;
            }

            if (input.equals("нет") || input.equals("no") || input.equals("n")) {
                return false;
            }

            System.out.println("Invalid input. Please enter 'yes' or 'no'.");
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("The list of options cannot be empty");
        }

        while (true) {
            System.out.println(message);

            for (int i = 0; i < options.size(); i++) {
                System.out.println("  [" + (i + 1) + "] " + options.get(i));
            }

            System.out.print("Choose number: ");

            String input = scanner.nextLine().trim();

            try {
                int choice = Integer.parseInt(input);

                if (choice < 1 || choice > options.size()) {
                    System.out.println("Invalid number. Select a number between 1 and " + options.size());
                    continue;
                }

                return options.get(choice - 1);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Try again.");
            }
        }
    }
}