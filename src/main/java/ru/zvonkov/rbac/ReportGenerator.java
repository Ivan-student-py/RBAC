package ru.zvonkov.rbac;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== USER REPORT ===\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users found\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s | %-25s | %-25s | %-30s | %-30s\n",
                "Username", "Full Name", "Email", "Roles", "Permissions"));
        sb.append("-".repeat(120)).append("\n");

        for (User user : users) {
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);

            List<String> roleNames = new ArrayList<>();
            for (RoleAssignment a : assignments) {
                roleNames.add(a.role().name());
            }
            String rolesStr = roleNames.isEmpty() ? "none" : String.join(", ", roleNames);

            Set<Permission> permissions = assignmentManager.getUserPermissions(user);
            List<String> permNames = new ArrayList<>();
            for (Permission p : permissions) {
                permNames.add(p.name() + ":" + p.resource());
            }
            String permsStr = permNames.isEmpty() ? "none" : String.join(", ", permNames);

            sb.append(String.format("%-20s | %-25s | %-25s | %-30s | %-30s\n",
                    user.username(),
                    user.fullName(),
                    user.email(),
                    rolesStr.length() > 30 ? rolesStr.substring(0, 27) + "..." : rolesStr,
                    permsStr.length() > 30 ? permsStr.substring(0, 27) + "..." : permsStr));
        }

        sb.append("\nTotal users: ").append(users.size());
        return sb.toString();
    }

    public String generateUserReportParallel(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== USER REPORT (PARALLEL) ===\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users found\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s | %-25s | %-25s | %-30s | %-30s\n",
                "Username", "Full Name", "Email", "Roles", "Permissions"));
        sb.append("-".repeat(120)).append("\n");

        // Параллельная обработка пользователей
        List<String> userLines = users.parallelStream()
                .map(user -> {
                    List<RoleAssignment> assignments = assignmentManager.findByUser(user);

                    List<String> roleNames = assignments.stream()
                            .map(a -> a.role().name())
                            .collect(Collectors.toList());
                    String rolesStr = roleNames.isEmpty() ? "none" : String.join(", ", roleNames);

                    Set<Permission> permissions = assignmentManager.getUserPermissions(user);
                    List<String> permNames = permissions.stream()
                            .map(p -> p.name() + ":" + p.resource())
                            .collect(Collectors.toList());
                    String permsStr = permNames.isEmpty() ? "none" : String.join(", ", permNames);

                    return String.format("%-20s | %-25s | %-25s | %-30s | %-30s",
                            user.username(),
                            user.fullName(),
                            user.email(),
                            rolesStr.length() > 30 ? rolesStr.substring(0, 27) + "..." : rolesStr,
                            permsStr.length() > 30 ? permsStr.substring(0, 27) + "..." : permsStr);
                })
                .collect(Collectors.toList());

        // Объединение строк (последовательно, т.к. порядок важен для читаемости)
        for (String line : userLines) {
            sb.append(line).append("\n");
        }

        sb.append("\nTotal users: ").append(users.size());
        return sb.toString();
    }

    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ROLE REPORT ===\n\n");

        List<Role> roles = roleManager.findAll();
        if (roles.isEmpty()) {
            sb.append("No roles found\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s | %-10s | %-15s | %-20s\n",
                "Role Name", "Users", "Permissions", "Description"));
        sb.append("-".repeat(75)).append("\n");

        for (Role role : roles) {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            int userCount = assignments.size();
            int permCount = role.getPermissions().size();

            sb.append(String.format("%-20s | %-10d | %-15d | %-20s\n",
                    role.name(),
                    userCount,
                    permCount,
                    role.description().length() > 20
                            ? role.description().substring(0, 17) + "..."
                            : role.description()));
        }

        sb.append("\nTotal roles: ").append(roles.size());
        return sb.toString();
    }

    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PERMISSION MATRIX ===\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users found\n");
            return sb.toString();
        }

        Set<String> resources = new TreeSet<>();
        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            for (Permission p : perms) {
                resources.add(p.resource());
            }
        }

        if (resources.isEmpty()) {
            sb.append("No permissions found\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s | ", "Username"));
        List<String> resourceList = new ArrayList<>(resources);
        for (int i = 0; i < resourceList.size(); i++) {
            String res = resourceList.get(i);
            sb.append(String.format("%-15s", res));
            if (i < resourceList.size() - 1) {
                sb.append(" | ");
            }
        }
        sb.append("\n");
        sb.append("-".repeat(22 + (17 * resourceList.size()))).append("\n");

        for (User user : users) {
            sb.append(String.format("%-20s | ", user.username()));

            Set<Permission> userPerms = assignmentManager.getUserPermissions(user);
            Set<String> userResources = new HashSet<>();
            for (Permission p : userPerms) {
                userResources.add(p.resource());
            }

            for (int i = 0; i < resourceList.size(); i++) {
                String res = resourceList.get(i);
                String mark = userResources.contains(res) ? "✓" : "✗";
                sb.append(String.format("%-15s", mark));
                if (i < resourceList.size() - 1) {
                    sb.append(" | ");
                }
            }
            sb.append("\n");
        }

        sb.append("\n✓ = has permission, ✗ = no permission\n");
        sb.append("Total users: ").append(users.size()).append(", Resources: ").append(resources.size());
        return sb.toString();
    }

    public String generatePermissionMatrixParallel(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PERMISSION MATRIX (PARALLEL) ===\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("No users found\n");
            return sb.toString();
        }

        // Сбор уникальных ресурсов (последовательно, т.к. их обычно мало)
        Set<String> resources = new TreeSet<>();
        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            for (Permission p : perms) {
                resources.add(p.resource());
            }
        }

        if (resources.isEmpty()) {
            sb.append("No permissions found\n");
            return sb.toString();
        }

        sb.append(String.format("%-20s | ", "Username"));
        List<String> resourceList = new ArrayList<>(resources);
        for (int i = 0; i < resourceList.size(); i++) {
            String res = resourceList.get(i);
            sb.append(String.format("%-15s", res));
            if (i < resourceList.size() - 1) {
                sb.append(" | ");
            }
        }
        sb.append("\n");
        sb.append("-".repeat(22 + (17 * resourceList.size()))).append("\n");

        // Параллельная обработка пользователей для матрицы
        List<String> matrixLines = users.parallelStream()
                .map(user -> {
                    StringBuilder line = new StringBuilder();
                    line.append(String.format("%-20s | ", user.username()));

                    Set<Permission> userPerms = assignmentManager.getUserPermissions(user);
                    Set<String> userResources = new HashSet<>();
                    for (Permission p : userPerms) {
                        userResources.add(p.resource());
                    }

                    for (int i = 0; i < resourceList.size(); i++) {
                        String res = resourceList.get(i);
                        String mark = userResources.contains(res) ? "✓" : "✗";
                        line.append(String.format("%-15s", mark));
                        if (i < resourceList.size() - 1) {
                            line.append(" | ");
                        }
                    }
                    return line.toString();
                })
                .collect(Collectors.toList());

        // Объединение строк (последовательно для сохранения порядка)
        for (String line : matrixLines) {
            sb.append(line).append("\n");
        }

        sb.append("\n✓ = has permission, ✗ = no permission\n");
        sb.append("Total users: ").append(users.size()).append(", Resources: ").append(resources.size());
        return sb.toString();
    }

    public void exportToFile(String report, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(report);
        }
    }
}