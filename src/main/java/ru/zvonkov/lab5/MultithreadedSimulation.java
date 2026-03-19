package ru.zvonkov.lab5;

import java.util.concurrent.atomic.AtomicInteger;

public class MultithreadedSimulation {

    // Параметры расчёта
    private static final int THREAD_COUNT = 5;           // Количество потоков
    private static final int CALCULATION_LENGTH = 20;    // Длина расчёта (шагов)
    private static final long STEP_DELAY_MS = 100;       // Задержка между шагами (мс)

    // Счётчик для нумерации потоков
    private static final AtomicInteger threadCounter = new AtomicInteger(1);

    // Класс для потока расчёта
    static class CalculationThread extends Thread {
        private final int threadNumber;
        private final int calculationLength;
        private final long stepDelay;

        public CalculationThread(int calculationLength, long stepDelay) {
            this.threadNumber = threadCounter.getAndIncrement();
            this.calculationLength = calculationLength;
            this.stepDelay = stepDelay;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();

            // Выполнение расчёта
            for (int i = 1; i <= calculationLength; i++) {
                try {
                    Thread.sleep(stepDelay);

                    // Вывод прогресса (кроме последнего шага)
                    if (i < calculationLength) {
                        synchronized (System.out) {
                            String progress = "#".repeat(i);
                            System.out.printf("[%d] Thread-%d: %s%n", threadNumber, getId(), progress);
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Финальный вывод (только один раз)
            synchronized (System.out) {
                String progress = "#".repeat(calculationLength);
                System.out.printf("[%d] Thread-%d: %s (100%%) - %.2fс%n",
                        threadNumber, getId(), progress, duration / 1000.0);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Многопоточная имитация расчёта ===");
        System.out.println("Потоков: " + THREAD_COUNT);
        System.out.println("Длина расчёта: " + CALCULATION_LENGTH + " шагов");
        System.out.println("Задержка: " + STEP_DELAY_MS + " мс на шаг");
        System.out.println();

        // Создание и запуск потоков
        CalculationThread[] threads = new CalculationThread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new CalculationThread(CALCULATION_LENGTH, STEP_DELAY_MS);
            threads[i].start();
        }

        // Ожидание завершения всех потоков
        for (CalculationThread thread : threads) {
            thread.join();
        }

        System.out.println("\n=== Расчёт завершён ===");
    }
}