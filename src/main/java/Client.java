import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket clientSocket = new Socket("127.0.0.1", 6379);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String serverResponse;
        while ((serverResponse = in.readLine()) != null) {
            System.out.println("Server says: " + serverResponse);
            // Process the received data
        }
    }
}
