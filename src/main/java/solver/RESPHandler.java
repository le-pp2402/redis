package solver;

import constants.DataType;
import constants.replication.Roles;
import container.TransactionManager;
import protocol.RespBuilder;
import utils.RedisInputStream;
import constants.Command;
import java.io.IOException;
import java.io.OutputStream;
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
    private static final Logger log = Logger.getLogger(RESPHandler.class);

    public void sendCommand(final OutputStream os, Pair<String, DataType> result) {
        try {
            if (result.second == DataType.INTEGER) {
                os.write(RespBuilder.integer(Integer.parseInt(result.first)).getBytes());
            } else if (result.second == DataType.ARRAYS) {
                if (result.first == null) {
                    os.write(RespBuilder.array(null).getBytes());
                } else {
                    os.write(result.first.getBytes());
                }
            } else {
                if (result.second == DataType.BULK_STRING) {
                    os.write(RespBuilder.bulkString(null).getBytes());
                } else if (result.second == DataType.SIMPLE_STRING) {
                    os.write(RespBuilder.simpleString(result.first).getBytes());
                } else if (result.second == DataType.ERROR) {
                    os.write(RespBuilder.error(result.first).getBytes());
                }
            }

            if (result.first != null && result.first.contains("FULLRESYNC")) {
                String emptyRDBFile = "UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";
                byte[] rdbBytes = Base64.getDecoder().decode(emptyRDBFile.getBytes(StandardCharsets.UTF_8));
                StringBuffer res = new StringBuffer();
                res.append((char) DOLLAR_BYTE);
                res.append(rdbBytes.length);
                res.append("\r\n");
                os.write(res.toString().getBytes());
                os.write(rdbBytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Pair<String, DataType> handle(RedisInputStream in, OutputStream out) {
        byte fb = in.readByte();
        return switch (fb) {
            case PLUS_BYTE -> handleSimpleString(in);
            case DOLLAR_BYTE -> handleBulkString(in);
            case ASTERISK_BYTE -> handleArray(in, out);
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

    private Pair<String, DataType> handleArray(RedisInputStream in, OutputStream out) {
        String inp = in.readLine();
        int len = Integer.parseInt(inp);
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

                    var parsedItems = new ArrayList<String>();
                    for (Pair<String, DataType> res : results) {
                        if (res.second.equals(DataType.ERROR)) {
                            parsedItems.add(RespBuilder.error(res.first));
                        } else if (res.second.equals(DataType.INTEGER)) {
                            parsedItems.add(RespBuilder.integer(Integer.parseInt(res.first)));
                        } else if (res.second.equals(DataType.BULK_STRING)) {
                            parsedItems.add(RespBuilder.bulkString(res.first));
                        } else if (res.second.equals(DataType.SIMPLE_STRING)) {
                            parsedItems.add(RespBuilder.simpleString(res.first));
                        } else {
                            log.info("Unsupported data type in transaction result: " + res.second);
                        }
                    }
                    return new Pair<>(RespBuilder.array(parsedItems), DataType.ARRAYS);
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
                if (cmd.equals(Command.SET) && Main.ROLE.equals(Roles.MASTER)) {
                    for (var outSlave : Main.slaves) {
                        try {
                            StringBuilder sb = new StringBuilder();
                            sb.append((char) ASTERISK_BYTE);
                            sb.append(args.size());
                            sb.append("\r\n");
                            for (String arg : args) {
                                sb.append((char) DOLLAR_BYTE);
                                sb.append(arg.length());
                                sb.append("\r\n");
                                sb.append(arg);
                                sb.append("\r\n");
                            }
                            log.info("Propagating command to slave: " + sb.toString());
                            outSlave.write(sb.toString().getBytes());
                            outSlave.flush();
                        } catch (IOException e) {
                            log.error("Failed to propagate command to slave: " + e.getMessage());
                        }
                    }
                }

                var res = Main.commandHandlers.get(cmd).handle(args.subList(1, args.size()));

                if (cmd.equals(Command.REPLCONF) && Main.ROLE.equals(Roles.MASTER) && !Main.slaves.contains(out)) {
                    Main.slaves.add(out);
                }

                return res;
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
