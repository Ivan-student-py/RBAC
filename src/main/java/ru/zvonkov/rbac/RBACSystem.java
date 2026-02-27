package ru.zvonkov.rbac;

public class RBACSystem {
    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private String currentUser;

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager(userManager, roleManager);
        this.currentUser = "system";
    }

    public UserManager getUserManager() { return userManager; }
    public RoleManager getRoleManager() { return roleManager; }
    public AssignmentManager getAssignmentManager() { return assignmentManager; }
    public String getCurrentUser() { return currentUser; }
    public void setCurrentUser(String currentUser) { this.currentUser = currentUser; }

    public void initialize() {
        Permission readUsers = new Permission("READ", "users", "Read user data");
        Permission writeUsers = new Permission("WRITE", "users", "Create and modify users");
        Permission deleteUsers = new Permission("DELETE", "users", "Delete users");

        Permission readRoles = new Permission("READ", "roles", "Read role definitions");
        Permission writeRoles = new Permission("WRITE", "roles", "Create and modify roles");
        Permission deleteRoles = new Permission("DELETE", "roles", "Delete roles");

        Permission readAssignments = new Permission("READ", "assignments", "View role assignments");
        Permission writeAssignments = new Permission("WRITE", "assignments", "Assign and revoke roles");

        Permission systemAccess = new Permission("ACCESS", "system", "Full system access");

        Role adminRole = new Role("Admin", "Full system administrator");
        adminRole.addPermission(readUsers);
        adminRole.addPermission(writeUsers);
        adminRole.addPermission(deleteUsers);
        adminRole.addPermission(readRoles);
        adminRole.addPermission(writeRoles);
        adminRole.addPermission(deleteRoles);
        adminRole.addPermission(readAssignments);
        adminRole.addPermission(writeAssignments);
        adminRole.addPermission(systemAccess);

        Role managerRole = new Role("Manager", "User and role manager");
        managerRole.addPermission(readUsers);
        managerRole.addPermission(writeUsers);
        managerRole.addPermission(readRoles);
        managerRole.addPermission(writeRoles);
        managerRole.addPermission(readAssignments);

        Role viewerRole = new Role("Viewer", "Read-only access");
        viewerRole.addPermission(readUsers);
        viewerRole.addPermission(readRoles);
        viewerRole.addPermission(readAssignments);

        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        try {
            User adminUser = User.validate("admin", "System Administrator", "admin@rbac.local");
            userManager.add(adminUser);

            // === 5. Назначение роли Admin администратору ===
            AssignmentMetadata meta = AssignmentMetadata.now("system", "Initial system setup");
            PermanentAssignment assignment = new PermanentAssignment(adminUser, adminRole, meta);
            assignmentManager.add(assignment);

            System.out.println("Система инициализирована: созданы роли, пользователь 'admin' и назначение.");
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации системы: " + e.getMessage());
        }
    }

    public String generateStatistics() {
        //
        return "Statistics not implemented yet";
    }
}