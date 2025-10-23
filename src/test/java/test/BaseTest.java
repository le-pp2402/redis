package test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BaseTest {
    public static List<String> forDebug(List<String> input) throws Exception {
        Socket clientSocket = new Socket("localhost", 6379);
        clientSocket.setSoTimeout(2000);
        OutputStream outputStream = clientSocket.getOutputStream();

        for (String cmd : input) {
            System.out.println("sending command...");
            try {
                outputStream.write(cmd.getBytes());
                outputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Reading server response (up to 2s timeout)...");
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String serverResponse;
        List<String> responses = new ArrayList<String>();
        while ((serverResponse = in.readLine()) != null) {
            System.out.println("Server says: " + serverResponse);
            responses.add(serverResponse);
        }

        clientSocket.close();

        return responses;
    }
}