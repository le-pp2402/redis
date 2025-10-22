package test.stream;

import java.net.Socket;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

public class XReadBlockedCommand {
    public static final Logger logger = Logger.getLogger(XReadBlockedCommand.class.getName());

    public static boolean done = false;

    @Test
    public void Test() {
        /*
         * XADD raspberry 0-1 temperature 68
         * XREAD BLOCK 1000 STREAMS raspberry 0-1
         */
        List<String> client1 = List.of(
                "*5\r\n$4\r\nXADD\r\n$9\r\nraspberry\r\n$3\r\n0-1\r\n$11\r\ntemperature\r\n$2\r\n68\r\n",
                "*6\r\n$5\r\nXREAD\r\n$5\r\nblock\r\n$4\r\n1000\r\n$7\r\nstreams\r\n$9\r\nraspberry\r\n$3\r\n0-1\r\n");

        /*
         * XADD raspberry 0-2 temperature 17
         */
        List<String> client2 = List
                .of("*5\r\n$4\r\nXADD\r\n$9\r\nraspberry\r\n$3\r\n0-2\r\n$11\r\ntemperature\r\n$2\r\n17\r\n");

        try {
            Socket clientSocket = new Socket("localhost", 6379);
            Socket clientSocket2 = new Socket("localhost", 6379);

            new Thread(() -> {
                try {
                    for (String cmd : client1) {
                        clientSocket.getOutputStream().write(cmd.getBytes());
                        clientSocket.getOutputStream().flush();
                    }
                    done = true;
                } catch (Exception e) {
                }
            }).start();

            new Thread(() -> {
                try {
                    while (!done) {
                        Thread.sleep(100);
                    }
                    logger.info("Client 2 sending XADD command...");
                    for (String cmd : client2) {
                        clientSocket2.getOutputStream().write(cmd.getBytes());
                        clientSocket2.getOutputStream().flush();
                    }
                } catch (Exception e) {
                }
            }).start();

        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
