//package ru.ifmo.ctddev.titova.webcrawler;
//
//import info.kgeorgiy.java.advanced.crawler.*;
//
//import java.io.IOException;
//import java.net.MalformedURLException;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Phaser;
//import java.util.concurrent.atomic.AtomicBoolean;
//
///**
// * Created by Sophia Titova on 29.03.17.
// */
//
///**
// * Provides crawling of web pages.
// */
//public class WebCrawler implements Crawler {
//
//    private final Downloader downloader;
//    private final ExecutorService downloaders, extractors;
//    private final Map<String, HostManager> countAndTaskPerHost;
//
//    private int hostLimit;
//
//    /**
//     * Creates new instance of web crawler.
//     *
//     * @param downloader  {@link Downloader} to use.
//     * @param downloaders Amount of threads for downloading.
//     * @param extractors  Amount of threads to extracting.
//     * @param perHost     Maximum amount of parallel downloads for one host.
//     */
//    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
//
//        this.downloader = downloader;
//        this.downloaders = Executors.newFixedThreadPool(downloaders);
//        this.extractors = Executors.newFixedThreadPool(extractors);
//
//        countAndTaskPerHost = new ConcurrentHashMap<>();
//        hostLimit = perHost;
//    }
//
//    /**
//     * Entering point of program during launching crawler from the console.
//     * <p>
//     * Using {@link CachingDownloader} as downloader.
//     * <p>
//     * Default depth is 2.
//     * <p>
//     * Default values for amount of threads is {@link Runtime#availableProcessors()}
//     * </p>
//     *
//     * @param args start url, amount of downloads, amount of extractors, maximum amount per host or default value.
//     */
//    public static void main(String[] args) {
//        try {
//            String s = args[0];
//            int p = Runtime.getRuntime().availableProcessors();
//            List<Integer> intArgs = new ArrayList<>(Collections.nCopies(3,
//                    p));
//            for (int i = 2; i < args.length; i++) {
//                intArgs.set(i - 2, Integer.parseInt(args[i]));
//            }
//            try (WebCrawler webCrawler = new WebCrawler(
//                    new CachingDownloader(), intArgs.get(0), intArgs.get(1), intArgs.get(2))) {
//                Result r = webCrawler.download(s, 1);
//                System.out.println("downloaded" + r.getDownloaded().size() + " errors" + r.getErrors().size());
//            } catch (IOException e) {
//                System.out.println("Error during creating an instance of CachingDownloader");
//            }
//        } catch (IndexOutOfBoundsException e) {
//            System.out.println("Missing starting link");
//        } catch (NullPointerException e) {
//            System.out.println("Invalid arguments, not expected null");
//        }
//    }
//
//    /**
//     * Crawls the given site parallel.
//     *
//     * @param url   Start Url
//     * @param depth Depth of crawl.
//     *              If 1 - download only given url.
//     *              If more than one - recursive downloads urls of the downloaded page.
//     * @return {@link Result} consisted of list of loaded urls and errors.
//     */
//    @Override
//    public Result download(String url, int depth) {
//
//        Set<String> result = ConcurrentHashMap.newKeySet(); /*new ConcurrentSkipListSet()*/
//        Map<String, IOException> errors = new ConcurrentHashMap<>();
//
//        Phaser p = new Phaser(1);
//        download(url, depth, result, errors, p);
//        p.arriveAndAwaitAdvance();
//
//        result.removeAll(errors.keySet());
//        return new Result(new ArrayList<>(result), errors);
//    }
//
//    private void download(String url, int depth, final Set<String> result, final Map<String, IOException> errors, Phaser p) {
//        try {
//            if (depth > 0 && result.add(url)) {
//                String host = URLUtils.getHost(url);
//                p.register();
//                checkAndSubmit(host, () -> {
//                    try {
//                        downloadHelper(url, depth, result, errors, p);
//                    } catch (IOException e) {
//                        errors.put(url, e);
//                    } finally {
//                        resolveHost(host);
//                        p.arrive();
//                    }
//                });
//            }
//        } catch (MalformedURLException e) {
//            errors.put(url, e);
//        }
//    }
//
//    private void checkAndSubmit(String host, Runnable task) {
//        AtomicBoolean f = new AtomicBoolean(true);
//        countAndTaskPerHost.putIfAbsent(host, new HostManager(0, new LinkedList<>()));
//        countAndTaskPerHost.compute(host, (k, v) -> {
//
//            if (f.getAndSet(false)) {
//                if (v == null) {
//                    v = new HostManager(0, new LinkedList<>());
//                }
//                if (v.getKey() < hostLimit) {
//                    downloaders.submit(task);
//                    return new HostManager(v.getKey() + 1, v.getValue());
//                }
//                v.getValue().add(task);
//
//                return v;
//            }
//            return  v;
//        });
//    }
//
//    private void downloadHelper(String s, int depth, final Set<String> result, final Map<String, IOException> errors, Phaser p)
//            throws IOException {
//
//        final Document doc = downloader.download(s);
//        if (depth == 1) {
//            return;
//        }
//        p.register();
//        extractors.submit(() -> {
//            try {
//                doc.extractLinks().forEach(x -> download(x, depth - 1, result, errors, p));
//            } catch (IOException e) {
//                errors.put(s, e);
//            } finally {
//                p.arrive();
//            }
//        });
//    }
//
//    private void resolveHost(String host) {
//        AtomicBoolean f = new AtomicBoolean(true);
//        countAndTaskPerHost.compute(host, (k, v) -> {
//            if (f.getAndSet(false)) {
//                if (!v.getValue().isEmpty()) {
//                    downloaders.submit(v.getValue().poll());
//                }
//                return new HostManager(v.getKey(), v.getValue());
//            }
//            return new HostManager(v.getKey() - 1, v.getValue());
//        });
//    }
//
//    /**
//     * Shutdowns all helper threads.
//     */
//    @Override
//    public void close() {
//        extractors.shutdown();
//        downloaders.shutdown();
//    }
//
//    private class HostManager {
//        int count;
//        Queue<Runnable> tasks;
//
//
//        HostManager(int count, Queue<Runnable> tasks) {
//            this.tasks = tasks;
//            this.count = count;
//        }
//
//        int getKey() {
//            return count;
//        }
//
//        Queue<Runnable> getValue() {
//            return tasks;
//        }
//    }
//}

package ru.ifmo.ctddev.titova.webcrawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;


/**
 * @author Titova Sophia
 *         Provides crawling of web pages.
 */
public class WebCrawler implements Crawler {

    private final Downloader downloader;
    private final ExecutorService downloaders, extractors;
    private final Map<String, HostManager> hostManagerMap;
    private Predicate<String> pred;
    private int hostLimit;

    /**
     * Creates new instance of web crawler.
     *
     * @param downloader  {@link Downloader} to use.
     * @param downloaders Amount of threads for downloading.
     * @param extractors  Amount of threads to extracting.
     * @param perHost     Maximum amount of parallel downloads for one host.
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this(downloader, downloaders, extractors, perHost, x -> true);
    }

    /**
     * Creates new instance of web crawler.
     *
     * @param downloader  {@link Downloader} to use.
     * @param downloaders Amount of threads for downloading.
     * @param extractors  Amount of threads to extracting.
     * @param perHost     Maximum amount of parallel downloads for one host.
     * @param p           Predicate to filter links.
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost, Predicate<String> p) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        hostManagerMap = new ConcurrentHashMap<>();
        this.pred = p;
        hostLimit = perHost;
    }

    /**
     * Entering point of program during launching crawler from the console.
     * <p>
     * Using {@link CachingDownloader} as downloader.
     * <p>
     * Default depth is 2.
     * <p>
     * Default values for amount of threads is {@link Runtime#availableProcessors()}
     * </p>
     *
     * @param args start url, amount of downloads, amount of extractors, maximum amount per host or default value.
     */
    public static void main(String[] args) {
        try {
            String s = args[0];
            int p = Runtime.getRuntime().availableProcessors();
            List<Integer> intArgs = new ArrayList<>(Collections.nCopies(3,
                    p));
            for (int i = 2; i < args.length; i++) {
                intArgs.set(i - 2, Integer.parseInt(args[i]));
            }
            try (WebCrawler webCrawler = new WebCrawler(
                    new CachingDownloader(), intArgs.get(0), intArgs.get(1), intArgs.get(2))) {
                Result r = webCrawler.download(s, 1);
//                Result r2 = webCrawler.download(s, 1);
//                Result r3 = webCrawler.download(s, 1);
                //System.out.println("downloaded" + r.getDownloaded().size() + " errors" + r.getErrors().size());
            } catch (IOException e) {
                System.out.println("Error during creating an instance of CachingDownloader");
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Missing starting link");
        } catch (NullPointerException e) {
            System.out.println("Invalid arguments, not expected null");
        }
    }

    /**
     * Crawls the given site parallel.
     *
     * @param url   Start Url
     * @param depth Depth of crawl.
     *              If 1 - download only given url.
     *              If more than one - recursive downloads urls of the downloaded page.
     * @return {@link Result} consisted of list of loaded urls and errors.
     */
    @Override
    public Result download(String url, int depth) {
        Set<String> result = ConcurrentHashMap.newKeySet(); /*new ConcurrentSkipListSet()*/
        Map<String, IOException> errors = new ConcurrentHashMap<>();

        Phaser p = new Phaser(1);
        System.out.println("");
        download(url, depth, result, errors, p);
        p.arriveAndAwaitAdvance();


        result.removeAll(errors.keySet());
        return new Result(new ArrayList<>(result), errors);
    }


    private void download(String url, int depth, final Set<String> result, final Map<String, IOException> errors, Phaser p) {
//        System.out.println(url);
        try {
            if (depth > 0 && pred.test(url) && result.add(url)) {
                System.out.println(url + " " + depth + " " + result.size());
                String host = URLUtils.getHost(url);
                p.register();
                checkAndSubmit(host, () -> {
                    try {
                        downloadHelper(url, depth, result, errors, p);
                    } catch (IOException e) {
                        e.printStackTrace();
                        errors.put(url, e);
                    } finally {
                        resolveHost(host);
                        p.arrive();
                    }
                });
            }

        } catch (MalformedURLException e) {
            errors.put(url, e);
        }
    }

    private void checkAndSubmit(String host, Runnable task) {
        hostManagerMap.putIfAbsent(host, new HostManager());
        HostManager hostL = hostManagerMap.get(host);
        hostL.lock();
        try {
            if (hostL.count < hostLimit) {
                downloaders.submit(task);
                hostL.count++;
            } else {
                hostL.tasks.add(task);
            }
        } finally {
            hostL.unlock();
        }
    }

    private void resolveHost(String host) {
        HostManager hostL = hostManagerMap.get(host);
        hostL.lock();
        try {
            if (!hostL.tasks.isEmpty()) {
                downloaders.submit(hostL.tasks.poll());

            } else {
                hostL.count--;
            }
        } finally {
            hostL.unlock();
        }
    }

    private void downloadHelper(String s, int i, final Set<String> result, final Map<String, IOException> errors, Phaser p)
            throws IOException {

        final Document doc = downloader.download(s);
        if (i == 1) {
            return;
        }
        p.register();
        extractors.submit(() -> {
            try {
                doc.extractLinks()
                        .stream()
                        .forEach(x -> download(x, i - 1, result, errors, p));
            } catch (IOException e) {
                errors.put(s, e);
            } finally {
                p.arrive();
            }
        });
    }


    /**
     * Shutdowns all helper threads.
     */
    @Override
    public void close() {
        extractors.shutdown();
        downloaders.shutdown();
    }

    private class HostManager {
        int count;
        Queue<Runnable> tasks;
        ReentrantLock lock;


        HostManager() {
            this.tasks = new ArrayDeque<>();
            this.count = 0;
            lock = new ReentrantLock();
        }

        void lock() {
            lock.lock();
        }

        void unlock() {
            lock.unlock();
        }

    }
}