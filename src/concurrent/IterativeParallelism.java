//package ru.ifmo.ctddev.titova.concurrent;//package ru.ifmo.ctddev.titova.concurrent;
//
//import info.kgeorgiy.java.advanced.concurrent.ListIP;
//import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
//
//import java.util.*;
//import java.util.function.Function;
//import java.util.function.Predicate;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
//
///**
// * Created by Sophia Titova on 11.03.17.
// */
//
///**
// * Class provides implementations of {@link ScalarIP} and {@link ListIP} interfaces.
// */
//public class IterativeParallelism implements ListIP, ScalarIP {
//
//    private ParallelMapper parallelMapper = null;
//
//    /**
//     * Creates an instance of {@link IterativeParallelism}.
//     */
//    public IterativeParallelism() {
//        parallelMapper = null;
//    }
//
//
//    /**
//     * Constructor to use ParallelMapper.
//     * <p>
//     * If use this constructor, all methods will use {@link ParallelMapper} instead
//     * of creating its own threads.
//     * <p>
//     * Parameter "numberOfThreads" in methods will be ignored.
//     * </p>
//     *
//     * @param parallelMapper {@link ParallelMapper} provides parallel applying function to args.
//     */
//    public IterativeParallelism(ParallelMapper parallelMapper) {
//        this.parallelMapper = parallelMapper;
//    }
//
//    /**
//     * Finds maximum in given {@link List}.
//     *
//     * @param threads    amount of threads
//     * @param list       source of data
//     * @param comparator {@link Comparator} to compare elements
//     * @param <T>        elements' type
//     * @return maximum in the list
//     * @throws InterruptedException             if thread has been interrupted.
//     * @throws java.util.NoSuchElementException If given <code>list</code> is empty.
//     */
//    @Override
//    public <T> T maximum(int threads, List<? extends T> list, Comparator<? super T> comparator)
//            throws InterruptedException {
//        Function<Stream<? extends T>, T> max = x -> x.max(comparator).get();
//        return concurrentApp(threads, list, max, max);
//    }
//
//    /**
//     * Finds minimum in given {@link List}.
//     *
//     * @param threads    amount of threads
//     * @param list       source of data
//     * @param comparator {@link Comparator} to compare elements
//     * @param <T>        elements' type
//     * @return minimum in the list
//     * @throws InterruptedException             if thread has been interrupted.
//     * @throws java.util.NoSuchElementException If given <code>list</code> is empty.
//     */
//    @Override
//    public <T> T minimum(int threads, List<? extends T> list, Comparator<? super T> comparator)
//            throws InterruptedException {
//        return maximum(threads, list, comparator.reversed());
//    }
//
//    /**
//     * Checks whether all elements in the given {@link List} satisfy given <code>predicate</code>.
//     *
//     * @param threads   amount of threads
//     * @param list      source of data
//     * @param predicate {@link Predicate} to test elements
//     * @param <T>       elements' type
//     * @return <tt>true</tt> if all elements match <code>predicate</code>, <tt>false</tt> otherwise
//     * @throws InterruptedException if thread has been interrupted.
//     */
//    @Override
//    public <T> boolean all(int threads, List<? extends T> list, Predicate<? super T> predicate)
//            throws InterruptedException {
//        return concurrentApp(threads, list,
//                x -> x.allMatch(predicate),
//                x -> x.allMatch(Boolean::booleanValue));
//    }
//
//    /**
//     * Checks whether some elements in the given {@link List} satisfy given <code>predicate</code>.
//     *
//     * @param threads   amount of threads
//     * @param list      source of data
//     * @param predicate {@link Predicate} to test elements
//     * @param <T>       elements' type
//     * @return <tt>true</tt> if all elements match <code>predicate</code>, <tt>false</tt> otherwise
//     * @throws InterruptedException if thread has been interrupted.
//     */
//    @Override
//    public <T> boolean any(int threads, List<? extends T> list, Predicate<? super T> predicate)
//            throws InterruptedException {
//        return !all(threads, list, predicate.negate());
//    }
//
//    /**
//     * Transforms all elements of given <code>list</code> by using <code>function</code>.
//     *
//     * @param threads  amount of threads
//     * @param list     source of data
//     * @param function {@link Function} to map elements
//     * @param <T>      type of source elements
//     * @param <U>      type of result elements
//     * @return mapped list
//     * @throws InterruptedException if thread has been interrupted.
//     */
//    @Override
//    public <T, U> List<U> map(int threads, List<? extends T> list, Function<? super T, ? extends U> function)
//            throws InterruptedException {
//        return concurrentApp(threads, list,
//                x -> x.map(function).collect(Collectors.toList()),
//                x -> x.flatMap(Collection::stream).collect(Collectors.toList()));
//    }
//
//    /**
//     * Filters all elements of given <code>list</code> by using <code>filter</code>.
//     *
//     * @param threads   amount of threads
//     * @param list      source of data
//     * @param predicate {@link Predicate} to filter elements
//     * @param <T>       type of source elements
//     * @return filtered list
//     * @throws InterruptedException if thread has been interrupted.
//     */
//    @Override
//    public <T> List<T> filter(int threads, List<? extends T> list, Predicate<? super T> predicate)
//            throws InterruptedException {
//        return concurrentApp(threads, list,
//                x -> x.filter(predicate).collect(Collectors.toList()),
//                x -> x.flatMap(Collection::stream).collect(Collectors.toList()));
//    }
//
//    /**
//     * Concatenates string representations of all elements of given <code>list</code> of elements into one {@link String}.
//     *
//     * @param threads amount of threads
//     * @param list    source of data
//     * @return result {@link String} consisting of all given {@link List} elements
//     * @throws InterruptedException if thread has been interrupted.
//     */
//    @Override
//    public String join(int threads, List<?> list) throws InterruptedException {
//        Function<Stream<?>, String> listJoin = x -> x.map(Object::toString).collect(Collectors.joining());
//        return concurrentApp(threads, list, listJoin, listJoin);
//    }
//
//    /**
//     * Provides parallel applying of function on given {@link List} of data by using {@link Thread}
//     * <p>
//     * Automatically started, managed and joined threads.
//     * </p>
//     * <p>
//     * Splits all given data on independent parts.
//     * applies function on this parts by using given amount of threads<code>threadsCount</code>.
//     * joins the results of applying by using <code>makeRes</code>.
//     * launches all tasks and awaits their finishing.
//     * </p>
//     *
//     * @param threadsCount amount of threads to use
//     * @param data         data to apply function on
//     * @param func         function to apply on independent part of data
//     * @param combiner     finalizing function to collect all results from data's parts
//     * @param <T>          the type of data
//     * @param <R>          the type of the result of applying function <code>func</code>
//     * @return the result of applying <code>func</code> and after that <code>combiner</code>
//     * @throws InterruptedException if thread has been interrupted
//     */
//    private <T, R> R concurrentApp(int threadsCount, List<? extends T> data,
//                                   final Function<Stream<? extends T>, R> func,
//                                   final Function<? super Stream<R>, R> combiner) throws InterruptedException {
//
//
//        List<Stream<? extends T>> lists = split(threadsCount, data);
//        List<R> funcRes;
//
//        if (parallelMapper != null) {
//            funcRes = parallelMapper.map(func, lists);
//        } else {
//
//            funcRes = new ArrayList<>(Collections.nCopies(threadsCount, null));
//            List<Thread> threads = new ArrayList<>();
//
//            for (int i = 0; i < lists.size(); i++) {
//                final int ind = i;
//                ParallelHelper.parallelStart(threads, (new Thread(() -> funcRes.set(ind, func.apply(lists.get(ind))))));
//            }
//            ParallelHelper.parallelClose(threads);
//
//        }
//        return combiner.apply(funcRes.stream());
//    }
//
//    /**
//     * Splits given {@link List} of data on independent parts.
//     *
//     * @param threadsCount the biggest amount of independent part that can be possible
//     * @param data         {@link List} of data to split
//     * @param <U>          data element's type
//     * @return List of independent parts of the given <code>data</code>, contains subLists of original list
//     */
//    private <U> List<Stream<? extends U>> split(int threadsCount, List<? extends U> data) {
//        List<Stream<? extends U>> independentParts = new ArrayList<>();
//        int length = (data.size()) / threadsCount;
//        int mod = data.size() % threadsCount;
//        int curStep = 0;
//        int perOne;
//        for (int i = 0; i < data.size(); i += perOne) {
//            perOne = length;
//            if (curStep < mod) {
//                perOne++;
//            }
//            curStep++;
//            independentParts.add(data.subList(i, i + perOne).stream());
//        }
//        return independentParts;
//    }
//}
