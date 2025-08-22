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
        outputStream.write("*5\r\n$3\r\nSET\r\n$9\r\npineapple\r\n$6\r\norange\r\n$2\r\npx\r\n$3\r\n100\r\n".getBytes());
        outputStream.write("*2\r\n$3\r\nGET\r\n$9\r\npineapple\r\n".getBytes());

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String serverResponse;
        while ((serverResponse = in.readLine()) != null) {
            System.out.println("Server says: " + serverResponse);
            // Process the received data
        }
    }
}
