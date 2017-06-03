package ru.ifmo.ctddev.titova.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;
import info.kgeorgiy.java.advanced.hello.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * @author Sophia Titova.
 *         UDP Server.
 *         Implementation of {@link HelloServer}.
 *         <p>
 *         Receives request from the client in form <tt>[prefix][threads]_[requests]</tt>.
 *         Responses <tt>Hello, [request]</tt> for <tt>[request]</tt>.
 */
public class HelloUDPServer implements HelloServer {

    private ServerHelper serverHelper;

    /**
     * Default constructor.
     * <p>
     * Creates an instance of HelloUDPServer.
     * </p>
     */
    public HelloUDPServer() {
    }

    /**
     * Entry point into program for starting with arguments from console.
     *
     * @param args Port number to listen for, amount of parallel working threads.
     * @see Thread
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2) {
            System.err.println("Not enough arguments");
            System.exit(-1);
        }
        new HelloUDPServer().start(UDPUtils.getArg(args[0], "port"),
                UDPUtils.getArg(args[1], "thread number"));
    }

    /**
     * Starts the server on the <code>port</code>.
     *
     * @param port    Port to listen for.
     * @param threads Number of worker threads.
     * @see Thread
     */
    @Override
    public void start(int port, int threads) {
        try {
            serverHelper = new ServerHelper(port, threads);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shutdowns all server threads, closes server.
     *
     * @see Thread
     */
    @Override
    public void close() {
        serverHelper.close();
    }

    private class ServerHelper implements AutoCloseable {

        final ExecutorService threadPool;
        private DatagramSocket socket;

        ServerHelper(int port, int threads) throws SocketException {
            threadPool = Executors.newFixedThreadPool(threads);
            socket = new DatagramSocket(port);
            final int bufSize = socket.getReceiveBufferSize();
            IntStream.range(0, threads).forEach(threadI -> {
                threadPool.submit(() -> {
                    try {
                        byte[] buf = new byte[bufSize];
                        DatagramPacket request = new DatagramPacket(buf, buf.length);
                        while (!Thread.interrupted()) {
                            socket.receive(request);
                            byte message[] = ("Hello, " + new String(request.getData(), request.getOffset(),
                                    request.getLength(), Util.CHARSET)).getBytes(Util.CHARSET);
                            socket.send(new DatagramPacket(message, message.length, request.getSocketAddress()));
                        }
                    } catch (PortUnreachableException e) {
                        System.out.println("socket is connected to a currently unreachable destination");

                    } catch (IOException e) {
                        System.out.println("I/O error: " + e.getMessage());
                    }
                });
            });
        }

        /**
         * Close all thread in {@link ExecutorService} and closes socket.
         */
        @Override
        public void close() {
            threadPool.shutdownNow();
            socket.close();
        }
    }
}
