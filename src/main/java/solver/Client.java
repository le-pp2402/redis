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
            "*5\r\n$4\r\nXADD\r\n$6\r\nbanana\r\n$3\r\n0-1\r\n$5\r\nmango\r\n$4\r\npear\r\n",
            "*5\r\n$4\r\nXADD\r\n$6\r\nbanana\r\n$3\r\n0-2\r\n$9\r\npineapple\r\n$4\r\npear\r\n",
            "*5\r\n$4\r\nXADD\r\n$6\r\nbanana\r\n$3\r\n0-3\r\n$5\r\napple\r\n$6\r\norange\r\n",
            "*5\r\n$4\r\nXADD\r\n$6\r\nbanana\r\n$3\r\n0-4\r\n$6\r\nbanana\r\n$5\r\ngrape\r\n",
            "*4\r\n$6\r\nXRANGE\r\n$6\r\nbanana\r\n$3\r\n0-2\r\n$3\r\n0-4\r\n"
        };

        for (int i = 0; i < 5; i++) {
            outputStream.write(data[i].getBytes());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        String serverResponse;
        while ((serverResponse = in.readLine()) != null) {
            System.out.println("Server says: " + serverResponse);
        }
    }
}
