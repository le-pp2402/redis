package solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket("127.0.0.1", 6379);

        OutputStream outputStream = clientSocket.getOutputStream();
        String[] data = {
                "*6\r\n$5\r\nXREAD\r\n$5\r\nblock\r\n$4\r\n1000\r\n$7\r\nstreams\r\n$5\r\napple\r\n$3\r\n0-1\r\n",
        };


        for (int i = 0; i < 4; i++) {
            outputStream.write(data[i].getBytes());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String serverResponse;
        while ((serverResponse = in.readLine()) != null) {
            System.out.println("Server says: " + serverResponse);
        }
    }
}
