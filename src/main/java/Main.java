import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    //  Uncomment this block to pass the first stage
        ServerSocket serverSocket = null;
        int port = 6379;
        try {
          serverSocket = new ServerSocket(port);
          // Since the tester restarts your program quite often, setting SO_REUSEADDR
          // ensures that we don't run into 'Address already in use' errors
          serverSocket.setReuseAddress(true);
          // Wait for connection from client.

            ExecutorService threadPool = new ThreadPoolExecutor(
                    4,                // corePoolSize
                    50,               // maximumPoolSize
                    60L,              // keepAliveTime
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>()
            );


            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            }

        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
        }
  }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String clientRequest;
            while ((clientRequest = in.readLine()) != null) {
                System.out.println("Client says: " + clientRequest);

                if (clientRequest.equals("PING")) {
                    out.write("+PONG\r\n".getBytes());
                } else {
                    out.write("-ERR unknown command\r\n".getBytes());
                }
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
