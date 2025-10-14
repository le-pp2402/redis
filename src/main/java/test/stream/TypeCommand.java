package test.stream;

import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

public class TypeCommand {
    private static final Logger logger = Logger.getLogger(TypeCommand.class.getName());

    void shouldReturnStreamWhenIsStreamContainterExists() {
        try {
            Socket clientSocket = new Socket("127.0.0.1", 0);
            OutputStream outputStream = clientSocket.getOutputStream();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
