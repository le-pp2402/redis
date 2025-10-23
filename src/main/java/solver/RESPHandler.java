package solver;

import constants.DataType;
import container.TransactionManager;
import utils.BinaryConverter;
import utils.RedisInputStream;
import constants.Command;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Queue;
import org.apache.log4j.Logger;

public class RESPHandler {
    public static final byte DOLLAR_BYTE = '$';
    public static final byte ASTERISK_BYTE = '*';
    public static final byte PLUS_BYTE = '+';
    public static final byte MINUS_BYTE = '-';
    public static final byte COLON_BYTE = ':';
    public static final byte[] CRLF = "\r\n".getBytes();

    private TransactionManager transactionManager = new TransactionManager();
    private Queue<Pair<Command, List<String>>> transactionQueue = new ArrayDeque<>();
    private static final Logger log = org.apache.log4j.Logger.getLogger(RESPHandler.class);

    public void sendCommand(final OutputStream os, Pair<String, DataType> result) {
        try {
            if (result.second == DataType.INTEGER) {
                var sb = new StringBuilder();
                sb.append((char) COLON_BYTE);
                sb.append(result.first);
                sb.append("\r\n");
                os.write(sb.toString().getBytes());
            } else if (result.second == DataType.ARRAYS) {
                if (result.first == null) {
                    os.write(("*-1\r\n").getBytes());
                } else {
                    os.write(result.first.getBytes());
                }
            } else {
                StringBuilder sb = new StringBuilder();

                sb.append((char) result.second.getSymbol());

                if (result.second != DataType.SIMPLE_STRING)

                    if (!(result.second == DataType.BULK_STRING && result.first.equals("-1"))
                            && result.second != DataType.ERROR) {
                        sb.append(result.first.length());
                        sb.append("\r\n");
                    }

                sb.append(result.first);
                sb.append("\r\n");

                os.write(sb.toString().getBytes());
            }

            if (result.first != null && result.first.contains("FULLRESYNC")) {
                String emptyRDBFile = "UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";
                byte[] rdbBytes = Base64.getDecoder().decode(emptyRDBFile.getBytes(StandardCharsets.UTF_8));

                StringBuffer binRDBFile = new StringBuffer();

                for (int i = 0; i < rdbBytes.length; i++) {
                    binRDBFile.append(BinaryConverter.toBinary(rdbBytes.toString()));
                }

                StringBuffer res = new StringBuffer();
                res.append((char) DOLLAR_BYTE);
                res.append(rdbBytes.length);
                res.append("\r\n");
                res.append(binRDBFile);
                os.write(res.toString().getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Pair<String, DataType> handle(RedisInputStream in) {
        byte fb = in.readByte();

        return switch (fb) {
            case PLUS_BYTE -> handleSimpleString(in);
            case DOLLAR_BYTE -> handleBulkString(in);
            case ASTERISK_BYTE -> handleArray(in);
            case COLON_BYTE -> handleInteger(in);
            case MINUS_BYTE -> handleError(in);
            default -> handle(in, fb);
        };
    }

    private Pair<String, DataType> handleError(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    private Pair<String, DataType> handleInteger(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    private Pair<String, DataType> handleArray(RedisInputStream in) {
        int len = Integer.parseInt(in.readLine());
        List<String> args = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            byte _ = in.readByte();
            int elemLen = Integer.parseInt(in.readLine());
            args.add(in.readBytes(elemLen));
            in.ensureCrLf();
        }

        log.info("Handling command with args: [");
        for (var elem : args) {
            log.info(elem);
        }
        log.info("]");

        if (!args.isEmpty()) {
            var cmd = Command.getCommand(args.get(0));

            if (cmd.equals(Command.MULTI)) {
                transactionManager.setCalledMulti(true);
            } else if (cmd.equals(Command.EXEC) && !transactionManager.isCalledMulti()) {
                return new Pair<>("ERR EXEC without MULTI", DataType.ERROR);
            } else if (cmd.equals(Command.EXEC) && transactionManager.isCalledMulti()) {
                transactionManager.setCalledMulti(false);
                if (transactionQueue.isEmpty()) {
                    return new Pair<>("*0\r\n", DataType.ARRAYS);
                } else {
                    List<Pair<String, DataType>> results = new ArrayList<>();
                    while (transactionQueue.peek() != null) {
                        var pair = transactionQueue.poll();
                        var command = pair.first;
                        var arguments = pair.second;
                        if (Main.commandHandlers.containsKey(command)) {
                            var result = Main.commandHandlers.get(command).handle(arguments);
                            results.add(result);
                            log.info("Executed command in transaction: " + command + " with result: " + result.first
                                    + " result type: " + result.second);
                        } else {
                            log.info("Unsupported command in transaction: " + command);
                        }
                    }

                    StringBuffer sb = new StringBuffer();
                    sb.append("*").append(results.size()).append("\r\n");
                    for (Pair<String, DataType> res : results) {
                        if (res.second.equals(DataType.ERROR)) {
                            sb.append((char) MINUS_BYTE).append(res.first).append("\r\n");
                        } else if (res.second.equals(DataType.INTEGER)) {
                            sb.append((char) COLON_BYTE).append(res.first).append("\r\n");
                        } else if (res.second.equals(DataType.BULK_STRING)) {
                            sb.append((char) DOLLAR_BYTE).append(res.first.length()).append("\r\n");
                            sb.append(res.first).append("\r\n");
                        } else if (res.second.equals(DataType.SIMPLE_STRING)) {
                            sb.append((char) PLUS_BYTE).append(res.first).append("\r\n");
                        } else {
                            log.info("Unsupported data type in transaction result: " + res.second);
                        }
                    }
                    return new Pair<>(sb.toString(), DataType.ARRAYS);
                }
            } else if (cmd.equals(Command.DISCARD) && transactionManager.isCalledMulti()) {
                transactionManager.setCalledMulti(false);
                transactionQueue.clear();
                return new Pair<>("OK", DataType.SIMPLE_STRING);
            } else if (cmd.equals(Command.DISCARD) && !transactionManager.isCalledMulti()) {
                return new Pair<>("ERR DISCARD without MULTI", DataType.ERROR);
            } else {
                if (transactionManager.isCalledMulti()) {
                    transactionQueue.add(new Pair<>(cmd, args.subList(1, args.size())));
                    for (Pair<Command, List<String>> pair : transactionQueue) {
                        log.info("Queued command in transaction: " + pair.first);
                    }
                    return new Pair<>("QUEUED", DataType.SIMPLE_STRING);
                }
            }

            if (Main.commandHandlers.containsKey(cmd)) {
                return Main.commandHandlers.get(cmd).handle(args.subList(1, args.size()));
            }
            throw new UnsupportedOperationException("This operation is not yet implemented.");
        }

        throw new UnsupportedOperationException("This operation is not yet implemented.");

    }

    public Pair<String, DataType> handle(RedisInputStream in, byte fb) {
        String req = (char) fb + in.readLine();

        Command cmd = Command.getCommand(req.substring(0, req.indexOf(' ')));

        var args = Arrays.stream(req.substring(req.indexOf(' ') + 1).split(" ")).toList();

        if (Main.commandHandlers.containsKey(cmd)) {
            return Main.commandHandlers.get(cmd).handle(args.subList(1, args.size()));
        }
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    private Pair<String, DataType> handleBulkString(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    public Pair<String, DataType> handleSimpleString(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }
}
