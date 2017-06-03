package ru.ifmo.ctddev.titova.hello;

import java.io.IOException;

/**
 * @author Sophia Titova.
 *         Launches {@link ru.ifmo.ctddev.titova.hello.HelloUDPServer}
 *         then launches {@link ru.ifmo.ctddev.titova.hello.HelloUDPClient}.
 *         <p>
 *         Used for testing.
 */
class Main {
    /**
     * Entry point for testing class {@link Main}.
     *
     * @param args ignored.
     * @throws InterruptedException when thread was interrupted.
     * @throws IOException          whet it was an i/o error.
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        HelloUDPServer server = new HelloUDPServer();
        server.start(8002, 2);
        Thread.sleep(500);
        HelloUDPClient client = new HelloUDPClient();
        client.run("127.0.0.1", 8002, "well done ", 2, 2);
        server.close();

    }
}