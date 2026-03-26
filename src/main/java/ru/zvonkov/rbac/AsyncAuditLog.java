package ru.zvonkov.rbac;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AsyncAuditLog {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Очередь для асинхронной обработки записей
    private final BlockingQueue<AuditEntry> queue = new LinkedBlockingQueue<>();

    // Хранилище завершённых записей (для чтения)
    private final List<AuditEntry> entries = new ArrayList<>();

    // Фоновый поток-обработчик
    private final Thread workerThread;

    // Флаг для корректного завершения
    private volatile boolean running = true;

    public AsyncAuditLog() {
        // Создаём и запускаем фоновый поток
        workerThread = new Thread(() -> {
            while (running || !queue.isEmpty()) {
                try {
                    // Ждём запись в очереди до 100 мс
                    AuditEntry entry = queue.poll(100, TimeUnit.MILLISECONDS);
                    if (entry != null) {
                        // Короткая синхронизация для добавления в список
                        synchronized (entries) {
                            entries.add(entry);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "AsyncAuditLog-Worker");

        workerThread.setDaemon(true); // Поток не блокирует завершение JVM
        workerThread.start();
    }

    /**
     * Асинхронно добавляет запись в лог
     */
    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        queue.offer(entry); // Неблокирующая вставка в очередь
    }

    /**
     * Возвращает все записи (синхронизировано для безопасности)
     */
    public List<AuditEntry> getAll() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    /**
     * Возвращает записи по исполнителю
     */
    public List<AuditEntry> getByPerformer(String performer) {
        synchronized (entries) {
            return entries.stream()
                    .filter(entry -> entry.performer().equals(performer))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Возвращает записи по действию
     */
    public List<AuditEntry> getByAction(String action) {
        synchronized (entries) {
            return entries.stream()
                    .filter(entry -> entry.action().equals(action))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Печатает лог в консоль
     */
    public void printLog() {
        List<AuditEntry> snapshot;
        synchronized (entries) {
            snapshot = new ArrayList<>(entries);
        }

        if (snapshot.isEmpty()) {
            System.out.println("Audit log is empty");
            return;
        }

        System.out.println("=== ASYNC AUDIT LOG ===");
        System.out.println("Total entries: " + snapshot.size());
        System.out.println();

        for (int i = 0; i < snapshot.size(); i++) {
            AuditEntry entry = snapshot.get(i);
            System.out.println("[" + (i + 1) + "] " + entry.timestamp());
            System.out.println("    Action:    " + entry.action());
            System.out.println("    Performer: " + entry.performer());
            System.out.println("    Target:    " + entry.target());
            System.out.println("    Details:   " + entry.details());
            System.out.println();
        }
    }

    /**
     * Сохраняет лог в файл
     */
    public void saveToFile(String filename) throws IOException {
        List<AuditEntry> snapshot;
        synchronized (entries) {
            snapshot = new ArrayList<>(entries);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("=== ASYNC AUDIT LOG ===\n");
            writer.write("Total entries: " + snapshot.size() + "\n\n");

            for (AuditEntry entry : snapshot) {
                writer.write("[" + entry.timestamp() + "] ");
                writer.write(entry.action() + " | ");
                writer.write(entry.performer() + " → " + entry.target());
                if (entry.details() != null && !entry.details().isEmpty()) {
                    writer.write(" | " + entry.details());
                }
                writer.write("\n");
            }
        }
    }

    /**
     * Корректно завершает работу логгера (обрабатывает оставшиеся записи)
     */
    public void shutdown() throws InterruptedException {
        running = false;
        workerThread.join(5000); // Ждём до 5 секунд завершения потока
        if (workerThread.isAlive()) {
            workerThread.interrupt();
        }
    }

    /**
     * Возвращает текущий размер очереди (для отладки)
     */
    public int getQueueSize() {
        return queue.size();
    }
}