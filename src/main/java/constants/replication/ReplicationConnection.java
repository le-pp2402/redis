package constants.replication;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

public class ReplicationConnection {
    private static final Logger log = Logger.getLogger(ReplicationConnection.class);

    private Socket socket;

    public ReplicationConnection(String masterAddress, int masterPort, int localPort) {
        try {
            this.socket = new Socket(masterAddress, masterPort);
            this.socket.setSoTimeout(2000);
        } catch (Exception e) {
            log.error("create socket failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
