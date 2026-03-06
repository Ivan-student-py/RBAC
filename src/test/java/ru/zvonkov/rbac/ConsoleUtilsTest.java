package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleUtilsTest {

    @Test
    void promptString_ValidInput_ReturnsInput() {
        String simulatedInput = "test input\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        String result = ConsoleUtils.promptString(scanner, "Enter text:", true);

        assertEquals("test input", result);

        System.setIn(System.in);
    }

    @Test
    void promptString_EmptyInput_RequiredField_RePrompts() {
        String simulatedInput = "\ntest\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        String result = ConsoleUtils.promptString(scanner, "Enter text:", true);

        assertEquals("test", result);

        System.setIn(System.in);
    }

    @Test
    void promptString_OptionalField_EmptyInput_ReturnsEmpty() {
        String simulatedInput = "\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        String result = ConsoleUtils.promptString(scanner, "Enter text:", false);

        assertEquals("", result);

        System.setIn(System.in);
    }

    @Test
    void promptInt_ValidInput_ReturnsInt() {
        String simulatedInput = "5\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        int result = ConsoleUtils.promptInt(scanner, "Enter number", 1, 10);

        assertEquals(5, result);

        System.setIn(System.in);
    }

    @Test
    void promptInt_InvalidInput_RePrompts() {
        String simulatedInput = "abc\n15\n7\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        int result = ConsoleUtils.promptInt(scanner, "Enter number", 1, 10);

        assertEquals(7, result);

        System.setIn(System.in);
    }

    @Test
    void promptYesNo_Yes_ReturnsTrue() {
        String simulatedInput = "yes\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        boolean result = ConsoleUtils.promptYesNo(scanner, "Confirm?");

        assertTrue(result);

        System.setIn(System.in);
    }

    @Test
    void promptYesNo_No_ReturnsFalse() {
        String simulatedInput = "no\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        boolean result = ConsoleUtils.promptYesNo(scanner, "Confirm?");

        assertFalse(result);

        System.setIn(System.in);
    }

    @Test
    void promptYesNo_ShortForms_Work() {
        String simulatedInput = "y\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        boolean result = ConsoleUtils.promptYesNo(scanner, "Confirm?");

        assertTrue(result);

        System.setIn(System.in);
    }

    @Test
    void promptChoice_ValidSelection_ReturnsChoice() {
        java.util.List<String> options = java.util.Arrays.asList("Option 1", "Option 2", "Option 3");

        String simulatedInput = "2\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        String result = ConsoleUtils.promptChoice(scanner, "Choose:", options);

        assertEquals("Option 2", result);

        System.setIn(System.in);
    }

    @Test
    void promptChoice_InvalidSelection_RePrompts() {
        java.util.List<String> options = java.util.Arrays.asList("Option 1", "Option 2");

        String simulatedInput = "abc\n5\n1\n";
        InputStream in = new ByteArrayInputStream(simulatedInput.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        String result = ConsoleUtils.promptChoice(scanner, "Choose:", options);

        assertEquals("Option 1", result);

        System.setIn(System.in);
    }

    @Test
    void promptChoice_EmptyList_ThrowsException() {
        java.util.List<String> emptyList = new java.util.ArrayList<>();

        InputStream in = new ByteArrayInputStream("".getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        assertThrows(IllegalArgumentException.class, () -> {
            ConsoleUtils.promptChoice(scanner, "Choose:", emptyList);
        });

        System.setIn(System.in);
    }
}