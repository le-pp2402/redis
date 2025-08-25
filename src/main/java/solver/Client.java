package solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        // First client connects to Redis
        Socket clientSocket = new Socket("127.0.0.1", 6379);
        OutputStream outputStream = clientSocket.getOutputStream();

        String[] data = {
                "*5\r\n$4\r\nXADD\r\n$9\r\nblueberry\r\n$3\r\n0-1\r\n$11\r\ntemperature\r\n$2\r\n11\r\n",
                "*6\r\n$5\r\nXREAD\r\n$5\r\nblock\r\n$1\r\n0\r\n$7\r\nstreams\r\n$9\r\nblueberry\r\n$1\r\n$\r\n",
                "*5\r\n$4\r\nXADD\r\n$9\r\nblueberry\r\n$3\r\n0-2\r\n$11\r\ntemperature\r\n$2\r\n46\r\n",
        };

        // First client sends an XADD command
        outputStream.write(data[0].getBytes());

        // First client sends XREAD BLOCK 0
        outputStream.write(data[1].getBytes());

        // Sleep for 500ms before second client sends data
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Second client connects to the SAME Redis port (fix here)
        Socket clientSocket1 = new Socket("127.0.0.1", 6379);
        clientSocket1.getOutputStream().write(data[2].getBytes());

        // Read server response for first client
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String serverResponse;
        while ((serverResponse = in.readLine()) != null) {
            System.out.println("Server says: " + serverResponse);
        }

        // Close sockets
        clientSocket1.close();
        clientSocket.close();
    }
}
