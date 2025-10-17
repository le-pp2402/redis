package test.stream;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import org.junit.jupiter.api.Test;

public class TypeCommand {

    @Test
    public void shouldReturnStreamWhenStreamContainterExists() throws Exception {
        // try {
        // Socket clientSocket = new Socket("localhost", 6379);

        // clientSocket.setSoTimeout(2000);

        // OutputStream outputStream = clientSocket.getOutputStream();

        // /*
        // * send: XADD banana 0-1 foo bar
        // * receive: +OK
        // */
        // System.out.println("sending XADD command...");
        // String insStat =
        // "*5\r\n$4\r\nXADD\r\n$6\r\nbanana\r\n$3\r\n0-1\r\n$3\r\nfoo\r\n$3\r\nbar\r\n";
        // outputStream.write(insStat.getBytes());
        // outputStream.flush();

        // /*
        // * send: TYPE banana
        // * receive: +stream
        // */
        // System.out.println("sending TYPE command...");
        // String typeStat = "*2\r\n$4\r\nTYPE\r\n$6\r\nbanana\r\n";
        // outputStream.write(typeStat.getBytes());
        // outputStream.flush();

        // System.out.println("Reading server response (up to 2s timeout)...");
        // BufferedReader in = new BufferedReader(new
        // InputStreamReader(clientSocket.getInputStream()));
        // String serverResponse;
        // while ((serverResponse = in.readLine()) != null) {
        // System.out.println("Server says: " + serverResponse);
        // }

        // clientSocket.close();

        // } catch (Exception e) {
        // e.printStackTrace();
        // }
    }
}
