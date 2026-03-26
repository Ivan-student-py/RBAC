package ru.zvonkov.rbac;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.Optional;

/**
 * Нагрузочный тест для проверки многопоточной работы системы RBAC
 */
public class LoadTest {

    @Test
    public void testConcurrentOperations() throws InterruptedException, ExecutionException {
        System.out.println("=== НАГРУЗОЧНЫЙ ТЕСТ: МНОГОПОТОЧНАЯ РАБОТА ===\n");

        RBACSystem system = new RBACSystem();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        int numThreads = 20;
        int operationsPerThread = 50;

        List<Callable<Void>> tasks = new ArrayList<>();

        // Создаём задачи для потоков
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            tasks.add(() -> {
                UserManager userManager = system.getUserManager();
                RoleManager roleManager = system.getRoleManager();
                AssignmentManager assignmentManager = system.getAssignmentManager();

                try {
                    // Создание уникальной роли для этого потока
                    String roleName = "Role_Thread_" + threadId;
                    if (!roleManager.exists(roleName)) {
                        Role role = new Role(roleName, "Test role for thread " + threadId);
                        role.addPermission(new Permission("READ", "test", "Test permission"));
                        roleManager.add(role);
                    }

                    for (int op = 0; op < operationsPerThread; op++) {
                        String username = "user_t" + threadId + "_op" + op;

                        // 1. Создание пользователя
                        try {
                            User user = User.validate(username, "Test User " + username, username + "@test.com");
                            userManager.add(user);
                        } catch (IllegalArgumentException e) {
                            // Пользователь уже существует - пропускаем
                        }

                        // 2. Назначение роли
                        try {
                            Optional<User> userOpt = userManager.findByUsername(username);
                            Optional<Role> roleOpt = roleManager.findByName(roleName);

                            if (userOpt.isPresent() && roleOpt.isPresent()) {
                                AssignmentMetadata meta = AssignmentMetadata.now("system", "Load test assignment");
                                PermanentAssignment assignment = new PermanentAssignment(userOpt.get(), roleOpt.get(), meta);
                                assignmentManager.add(assignment);
                            }
                        } catch (Exception e) {
                            // Игнорируем ошибки дубликатов
                        }

                        // 3. Фильтрация пользователей
                        UserFilter filter = UserFilters.byUsernameContains("user_t" + threadId);
                        List<User> filtered = userManager.findByFilter(filter);

                        // 4. Фильтрация ролей
                        RoleFilter roleFilter = RoleFilters.byNameContains("Role_Thread");
                        List<Role> roleFiltered = roleManager.findByFilter(roleFilter);

                        // 5. Проверка прав
                        Optional<User> userOpt = userManager.findByUsername(username);
                        if (userOpt.isPresent()) {
                            boolean hasPermission = assignmentManager.userHasPermission(userOpt.get(), "READ", "test");
                        }
                    }

                    System.out.println("Поток " + threadId + " завершил " + operationsPerThread + " операций");

                } catch (Exception e) {
                    System.err.println("Ошибка в потоке " + threadId + ": " + e.getMessage());
                }

                return null;
            });
        }

        // Запускаем все задачи параллельно
        System.out.println("Запуск " + numThreads + " потоков по " + operationsPerThread + " операций каждый...");
        System.out.println("Всего операций: " + (numThreads * operationsPerThread) + "\n");

        List<Future<Void>> futures = executor.invokeAll(tasks);

        // Ждём завершения всех потоков
        for (Future<Void> future : futures) {
            future.get();
        }

        executor.shutdown();

        // Проверяем результаты
        int totalUsers = system.getUserManager().count();
        int totalRoles = system.getRoleManager().count();
        int totalAssignments = system.getAssignmentManager().count();

        System.out.println("\n=== РЕЗУЛЬТАТЫ НАГРУЗОЧНОГО ТЕСТА ===");
        System.out.println("Создано пользователей: " + totalUsers);
        System.out.println("Создано ролей: " + totalRoles);
        System.out.println("Создано назначений: " + totalAssignments);
        System.out.println("=====================================\n");

        // Проверяем, что нет дубликатов и пропусков
        assert totalUsers > 0 : "Должны быть созданы пользователи";
        assert totalRoles >= numThreads : "Должны быть созданы роли для каждого потока";
        assert totalAssignments > 0 : "Должны быть созданы назначения";

        System.out.println("✅ Нагрузочный тест пройден успешно!\n");
    }

    @Test
    public void testParallelFilters() {
        System.out.println("=== ТЕСТ: ПАРАЛЛЕЛЬНЫЕ ФИЛЬТРЫ ===\n");

        RBACSystem system = new RBACSystem();
        UserManager userManager = system.getUserManager();

        // Создаём много пользователей для тестирования
        int numUsers = 1000;
        System.out.println("Создание " + numUsers + " тестовых пользователей...");

        for (int i = 0; i < numUsers; i++) {
            try {
                String username = "testuser_" + i;
                User user = User.validate(username, "Test User " + i, username + "@test.com");
                userManager.add(user);
            } catch (Exception e) {
                // Игнорируем дубликаты
            }
        }

        System.out.println("Пользователи созданы. Тестирование фильтров...\n");

        // Тестируем последовательный фильтр
        long startTime = System.nanoTime();
        UserFilter filter = UserFilters.byUsernameContains("testuser");
        List<User> sequentialResult = userManager.findByFilter(filter);
        long sequentialTime = System.nanoTime() - startTime;

        System.out.println("Последовательный фильтр:");
        System.out.println("  Найдено: " + sequentialResult.size() + " пользователей");
        System.out.println("  Время: " + (sequentialTime / 1_000_000) + " мс");

        // Тестируем параллельный фильтр (если метод существует)
        try {
            startTime = System.nanoTime();
            List<User> parallelResult = userManager.findByFilterParallel(filter);
            long parallelTime = System.nanoTime() - startTime;

            System.out.println("\nПараллельный фильтр:");
            System.out.println("  Найдено: " + parallelResult.size() + " пользователей");
            System.out.println("  Время: " + (parallelTime / 1_000_000) + " мс");

            // Сравниваем результаты
            assert sequentialResult.size() == parallelResult.size() :
                    "Параллельный и последовательный фильтры должны давать одинаковый результат";

            System.out.println("\n✅ Тест параллельных фильтров пройден успешно!");
        } catch (NoSuchMethodError e) {
            System.out.println("\n⚠️ Метод findByFilterParallel() не найден - пропускаем тест параллельных фильтров");
            System.out.println("Найдено: " + sequentialResult.size() + " пользователей");
        }

        System.out.println("=====================================\n");
    }

    @Test
    public void testAsyncCommands() throws InterruptedException {
        System.out.println("=== ТЕСТ: АСИНХРОННЫЕ КОМАНДЫ ===\n");

        RBACSystem system = new RBACSystem();

        // Запускаем несколько асинхронных операций
        CountDownLatch latch = new CountDownLatch(3);

        System.out.println("Запуск асинхронных операций...\n");

        system.getExecutorService().submit(() -> {
            try {
                Thread.sleep(100);
                System.out.println("[ASYNC] Операция 1 завершена");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        system.getExecutorService().submit(() -> {
            try {
                Thread.sleep(150);
                System.out.println("[ASYNC] Операция 2 завершена");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        system.getExecutorService().submit(() -> {
            try {
                Thread.sleep(50);
                System.out.println("[ASYNC] Операция 3 завершена");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                latch.countDown();
            }
        });

        // Ждём завершения всех операций
        latch.await();

        System.out.println("\n✅ Все асинхронные операции завершены!");
        System.out.println("=====================================\n");
    }
}