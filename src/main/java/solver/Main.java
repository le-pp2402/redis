package solver;

import constants.Command;
import constants.replication.Roles;
import container.ReplicationInfo;
import solver.impl.*;
import utils.RedisInputStream;
import utils.builds.RESPBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class Main {
    private final static Logger logger = Logger.getLogger(Main.class);

    public static Roles ROLE = Roles.MASTER;
    public static ReplicationInfo replicationInfo = new ReplicationInfo();

    public static void main(String[] args) {
        // Loading logger configurator;
        BasicConfigurator.configure();

        logger.info("Loading commands handler");
        loadCommandHandlers();

        logger.info("Logs from your program will appear here!");
        // Uncomment this block to pass the first stage
        ServerSocket serverSocket;

        int port = 6379;

        String masterHost = null;

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("--port")) {
                    if (i + 1 < args.length) {
                        port = Integer.parseInt(args[i + 1]);
                    } else {
                        logger.error("Port number not specified after --port=");
                    }
                }

                if (args[i].startsWith("--replicaof")) {
                    if (i + 1 < args.length) {
                        masterHost = args[i + 1];
                        ROLE = Roles.SLAVE;

                        String infos[] = masterHost.split("[ ]");
                        String masterAddress = infos[0], masterPort = infos[1];
                        try (Socket clientSocket = new Socket(masterAddress, Integer.parseInt(masterPort))) {
                            clientSocket.setSoTimeout(2000);
                            var outputStream = clientSocket.getOutputStream();
                            outputStream
                                    .write(RESPBuilder.buildArray(List.of(
                                            RESPBuilder.buildBulkString("PING"))).toString()
                                            .getBytes());
                            outputStream.flush();

                            outputStream
                                    .write(RESPBuilder.buildArray(List.of(
                                            RESPBuilder.buildBulkString("REPLCONF"),
                                            RESPBuilder.buildBulkString("listening-port"),
                                            RESPBuilder.buildBulkString(String.valueOf(port)))).toString()
                                            .getBytes());
                            outputStream.flush();

                            outputStream.write(RESPBuilder.buildArray(List.of(
                                    RESPBuilder.buildBulkString("REPLCONF"),
                                    RESPBuilder.buildBulkString("capa"),
                                    RESPBuilder.buildBulkString("psync2"))).toString()
                                    .getBytes());
                            outputStream.flush();

                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(clientSocket.getInputStream()));
                            String serverResponse;
                            while ((serverResponse = in.readLine()) != null) {
                                logger.info("Response from master: " + serverResponse);
                            }
                        } catch (Exception e) {
                            logger.error("Cannot connect to master " + masterAddress + ":" + masterPort);
                        }
                    } else {
                        logger.error("Master host not specified after --replicaof=");
                    }
                }
            }
        }

        logger.info("Starting server on port " + port);

        try {
            serverSocket = new ServerSocket(port);
            // Since the tester restarts your program quite often, setting SO_REUSEADDR
            // ensures that we don't run into 'Address already in use' errors
            serverSocket.setReuseAddress(true);
            // Wait for connection from client.

            try (ExecutorService threadPool = new ThreadPoolExecutor(
                    4,
                    50,
                    60L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>())) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> handleClient(clientSocket));
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static HashMap<Command, ICommandHandler> commandHandlers = new HashMap<>();

    private static void loadCommandHandlers() {
        commandHandlers.put(Command.PING, new Ping());
        commandHandlers.put(Command.ECHO, new Echo());
        commandHandlers.put(Command.GET, new Get());
        commandHandlers.put(Command.SET, new Set());
        commandHandlers.put(Command.TYPE, new Type());
        commandHandlers.put(Command.XADD, new XAdd());
        commandHandlers.put(Command.XRANGE, new XRange());
        commandHandlers.put(Command.XREAD, new XRead());
        commandHandlers.put(Command.INCR, new Incr());
        commandHandlers.put(Command.MULTI, new Multi());
        commandHandlers.put(Command.INFO, new Info());
        commandHandlers.put(Command.REPLCONF, new Replconf());
    }

    private static void handleClient(Socket clientSocket) {
        try (Socket socket = clientSocket) {
            var inputStream = socket.getInputStream();
            var outputStream = socket.getOutputStream();
            RedisInputStream redisInputStream = new RedisInputStream(inputStream);
            var respHandler = new RESPHandler();

            while (true) {
                try {
                    var result = respHandler.handle(redisInputStream);
                    respHandler.sendCommand(outputStream, result);
                } catch (RuntimeException e) {
                    logger.error(e.getMessage());
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
