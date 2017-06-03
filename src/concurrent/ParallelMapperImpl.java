//package ru.ifmo.ctddev.titova.concurrent;//package ru.ifmo.ctddev.titova.concurrent;
//
//import java.util.*;
//import java.util.function.Function;
//import java.util.stream.IntStream;
//
//import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
//
///**
// * Provides implementation for {@link ParallelMapper}.
// */
//public class ParallelMapperImpl implements ParallelMapper {
//
//    /**
//     * {@link List} of threads.
//     *
//     * @see Thread
//     */
//    private final List<Thread> threadPool;
//
//    /**
//     * {@link List} of tasks for threads parametrised by {@link Runnable}
//     *
//     * @see Thread
//     */
//    private final Queue<Runnable> tasksPool = new LinkedList<>();
//
//    /**
//     * Constructor creates new instance of mapper.
//     * <p>
//     * Creates {@link List} of <code>threads</code> to provide parallelisation.
//     * </p>
//     *
//     * @param threads amount of threads to use.
//     */
//    public ParallelMapperImpl(int threads) {
//
//        threadPool = new ArrayList<>(threads);
//
//        IntStream.range(0, threads).forEach(ind ->
//                ParallelHelper.parallelStart(threadPool, new Thread(() -> {
//                    try {
//                        Runnable task;
//                        while (!Thread.interrupted()) {
//                            synchronized (tasksPool) {
//                                while (tasksPool.isEmpty()) {
//                                    tasksPool.wait();
//                                }
//                                task = tasksPool.poll();
//                            }
//                            task.run();
//                        }
//                    } catch (InterruptedException e) {
//                        //e.printStackTrace();
//                    } finally {
//                        Thread.currentThread().interrupt();
//                    }
//                })));
//    }
//
//    /**
//     * Provides parallel applying of function for each element of given {@link List}.
//     *
//     * @param f    {@link Function} to apply.
//     * @param list {@link List} of arguments to apply function on.
//     * @param <T>  Source elements type.
//     * @param <R>  Type of elements of result list.
//     * @return Mapped {@link List} of elements which are results of applying given function.
//     * @throws InterruptedException If current thread has been interrupted during work.
//     */
//    @Override
//    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> list) throws InterruptedException {
//
//        final Info info = new Info();
//        final List<R> resultsPool = new ArrayList<>(Collections.nCopies(list.size(), null));
//        synchronized (tasksPool) {
//            IntStream.range(0, list.size()).forEach(ind -> {
//                        tasksPool.add(() -> {
//                                    resultsPool.set(ind, f.apply(list.get(ind)));
//                                    synchronized (info) {
//                                        if (++info.finish == list.size()) {
//                                            info.notify();
//                                        }
//                                    }
//                                }
//                        );
//                        tasksPool.notify();
//                    }
//            );
//        }
//
//
//        synchronized (info) {
//            while (info.finish < list.size()) {
//                info.wait();
//            }
//        }
//
//        return resultsPool;
//    }
//
//    /**
//     * Closes all workers threads.
//     *
//     * @throws InterruptedException If current thread has been interrupted while waiting workers to finish.
//     */
//    @Override
//    public void close() throws InterruptedException {
//        threadPool.forEach(Thread::interrupt);
//        ParallelHelper.parallelClose(threadPool);
//    }
//
//    /**
//     * Notifies when all results are given from each thread.
//     */
//    private static class Info {
//
//        /**
//         * Counter of threads finished applying function
//         */
//        private int finish = 0;
//
//    }
//}
