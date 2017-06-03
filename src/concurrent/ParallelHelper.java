package ru.ifmo.ctddev.titova.concurrent;

import java.util.List;


/**
 * Provides parallel starting and closing of threads.
 * @see Thread
 */
public class ParallelHelper {

    /**
     * Provides adding each thread to given {@link List} and starting each thread.
     * @param threads list to add thread.
     * @param thread current thread.
     */
    public static void parallelStart(List<Thread> threads, Thread thread) {
        threads.add(thread);
        thread.start();
    }

    /**
     * Provides closing for each thread of given {@link List}.
     * @param threads list of threads to close.
     * @throws InterruptedException If current thread has been interrupted during work.
     */
    public static void parallelClose(List<Thread> threads) throws InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
    }
}

