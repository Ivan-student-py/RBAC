package ru.zvonkov.lab5;

public class MultithreadedSimulation {

    private static final int THREAD_COUNT = 5;
    private static final int CALCULATION_LENGTH = 20;
    private static final int DELAY_MS = 100;
    private static final Object outputLock = new Object();

    public static void main(String[] args) {

        System.out.print("\033[2J\033[H");
        System.out.flush();

        System.out.println("=== Многопоточная имитация расчёта ===\n");
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        System.out.print("\033[" + (THREAD_COUNT + 4) + ";1H");
        System.out.println("[Расчёт в процессе...]");
        System.out.println("\033[2;6H");
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        int[] progress = new int[THREAD_COUNT];
        long[] threadIds = new long[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            System.out.printf("  [%d] Thread-?? | [%s]%n",
                    i + 1, createEmptyProgressBar());
        }
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadIndex = i;
            final int threadNum = i + 1;

            threads[i] = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                long threadId = Thread.currentThread().getId();

                synchronized (outputLock) {
                    threadIds[threadIndex] = threadId;
                }

                for (int pos = 1; pos <= CALCULATION_LENGTH; pos++) {
                    try {
                        Thread.sleep(DELAY_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    synchronized (outputLock) {
                        progress[threadIndex] = pos;

                        int lineNumber = 3 + threadIndex;
                        System.out.print("\033[" + lineNumber + ";1H");

                        String progressBar = createProgressBar(pos);

                        System.out.printf("  [%d] Thread-%d | [%s]%s",
                                threadNum, threadId, progressBar,
                                "\033[K");
                        System.out.flush();
                    }
                }

                long totalTime = System.currentTimeMillis() - startTime;

                synchronized (outputLock) {
                    int lineNumber = 3 + threadIndex;
                    System.out.print("\033[" + lineNumber + ";1H");

                    String progressBar = createProgressBar(CALCULATION_LENGTH);
                    System.out.printf("  [%d] Thread-%d | [%s] (100%%) - %.2fс %s",
                            threadNum, threadId, progressBar, totalTime / 1000.0,
                            "\033[K");
                    System.out.flush();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try { Thread.sleep(500); } catch (InterruptedException e) {}
        System.out.print("\033[" + (THREAD_COUNT + 4) + ";1H");
        System.out.println("[Все расчёты завершены.]");
    }

    private static String createProgressBar(int current) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CALCULATION_LENGTH; i++) {
            sb.append(i < current ? "#" : "-");
        }
        return sb.toString();
    }

    private static String createEmptyProgressBar() {
        return "-".repeat(CALCULATION_LENGTH);
    }
}