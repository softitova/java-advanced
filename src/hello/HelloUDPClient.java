package ru.ifmo.ctddev.titova.hello;
//

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.Util;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author Sophia Titova.
 *         UDP Client.
 *         Implementation of {@link HelloClient}.
 *         <p>
 *         Sends request to the server in form <tt>[prefix][threadN]_[requestN]</tt>.
 *         Waits for response in form <tt>Hello, [prefix][threadN]_[requestN]</tt>.
 *         </p>
 */
public class HelloUDPClient implements HelloClient {


    /**
     * Default constructor.
     * <p>Creates an instance of HelloUDPClient.</p>
     */
    public HelloUDPClient() {

    }

    /**
     * Entry point into program for starting with arguments from console.
     *
     * @param args server address, port number, prefix, thread number, request number.
     */
    public static void main(String[] args) {
        if (args == null || args.length < 5) {
            System.err.println("Not enough arguments");
            System.exit(-1);
        }
        String host = args[0];
        int port = UDPUtils.getArg(args[1], "port");
        String prefix = args[2];
        int threadN = UDPUtils.getArg(args[3], "thread number");
//        System.out.println(threadN + " here ");
        int requestN = UDPUtils.getArg(args[4], "requests number");

        new HelloUDPClient().run(host, port, prefix, requestN, threadN);
    }


    /**
     * Starts the UDPClient and awaits delivering of all requests.
     *
     * @param host     Address of the server.
     * @param port     Port.
     * @param prefix   Requests prefix.
     * @param requests Number of requests for one {@link Thread}.
     * @param threads  Number of parallel threads.
     */
    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {
        try {
            ClientHelper c = new ClientHelper(threads, host, port, prefix, requests);
            c.close();
        } catch (InterruptedException e) {
            System.err.println("Interrupted");
        } catch (UnknownHostException e) {
            System.err.println("Unknown host");
        }
    }


    private class ClientHelper {

        private static final int TIMER_MIN = 100;
        private static final int TIMER_MAX = 100000;
        private static final int TIMEOUT = 1000;
        private static final int LIM = 10;
        private Thread pool[];


        ClientHelper(int threads, String host, int port, String prefix, int requests) throws UnknownHostException {
            InetSocketAddress serverAddress = new InetSocketAddress(host, port);
            pool = IntStream.range(0, threads).mapToObj(threadI -> new Thread(() -> {
                MyTimer tm = new MyTimer(TIMER_MIN, TIMER_MAX, TIMEOUT, LIM);
                try (DatagramSocket sock = new DatagramSocket()) {
                    sock.setSoTimeout(tm.timeout);
                    byte[] buf = new byte[sock.getReceiveBufferSize()];
                    DatagramPacket response = new DatagramPacket(buf, buf.length);
                    IntStream.range(0, requests).forEach(requestN -> {
                        String requestStr = String.format("%s%d_%d", prefix, threadI, requestN);
                        byte message[] = requestStr.getBytes(Util.CHARSET);
                        DatagramPacket request = new DatagramPacket(message, message.length, serverAddress);
                        while (true) {
                            try {
                                sock.send(request);
                                sock.receive(response);
                                String s = new String(response.getData(), 0, response.getLength(), Util.CHARSET);
                                if (s.contains(requestStr)) {
                                    System.out.println(requestStr + "     :    " + s);
                                    return;
                                }
                                tm.update(1);
                            } catch (SocketTimeoutException e) {
                                tm.update(-1);
                                System.out.println("timeout exception, current timeout " + tm.timeout + " timeout missing count " + tm.counterFail);
                            } catch (PortUnreachableException e) {
                                System.out.println("socket is connected to a currently unreachable destination " + e.getMessage());
                            } catch (IOException e) {
                                System.out.println("I/O error " + e.getMessage());
                            }
                        }
                    });
                } catch (SocketException e) {
                    System.out.println("if the socket could not be opened, " +
                            "or the socket could not bind to the specified local port");
                }
            })).toArray(Thread[]::new);
            Arrays.stream(pool).forEach(Thread::start);
        }


        private void close() throws InterruptedException {
            for (Thread thread : pool) {
                thread.join();
            }
        }

        private class MyTimer {

            private final int timerMin;
            private final int timerMax;
            private final int limit;
            private int timeout;
            private int counterFail = 0;

            MyTimer(int timerMin, int timerMax, int timeout, int limit) {
                this.timerMin = timerMin;
                this.timerMax = timerMax;
                this.timeout = timeout;
                this.limit = limit;
            }

            void update(int i) {
                if (counterFail * i > 0) {
                    counterFail = 0;
                } else if (counterFail * (-i) < limit) {
                    counterFail -= i;
                } else {
                    counterFail = 0;
                    timeout = Math.max(i < 0 ? timeout * 2 : timeout / 2, i < 0 ? timerMax : timerMin);
                }
            }
        }
    }

//            void updateSuc() {
//                if (counterFail > 0) {
//                    counterFail = 0;
//                    return;
//                }
//                if (-counterFail < limit) {
//                    counterFail--;
//                } else {
//                    counterFail = 0;
//                    timeout = Math.max(timeout / 2, timerMin);
//                }
//            }
//
//            void updateFail() {
//                if (counterFail < 0) {
//                    counterFail = 0;
//                    return;
//                }
//                if (counterFail < limit) {
//                    counterFail++;
//                } else {
//                    counterFail = 0;
//                    timeout = Math.max(timeout * 2, timerMax);
//                }
//            }
}
