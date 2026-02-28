package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class CommandIntegrationTest {

    @Test
    void userCreateAndViewFlow() {
        RBACSystem system = new RBACSystem();
        CommandParser parser = new CommandParser();
        CommandRegistry.registerAllCommands(parser);

        String input = "testuser\ntest user\ntest@example.com\n";
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        Scanner scanner = new Scanner(System.in);

        parser.executeCommand("user-create", scanner, system);

        assertTrue(system.getUserManager().findByUsername("testuser").isPresent());

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        parser.executeCommand("user-view", new Scanner("testuser\n"), system);

        String output = outContent.toString();
        assertTrue(output.contains("testuser"));
        assertTrue(output.contains("test user"));
        assertTrue(output.contains("test@example.com"));

        System.setIn(System.in);
        System.setOut(System.out);
    }
}