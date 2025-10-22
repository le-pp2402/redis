package solver;

import constants.DataType;
import utils.RedisInputStream;
import constants.Command;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RESPHandler {
    public static final byte DOLLAR_BYTE = '$';
    public static final byte ASTERISK_BYTE = '*';
    public static final byte PLUS_BYTE = '+';
    public static final byte MINUS_BYTE = '-';
    public static final byte COLON_BYTE = ':';
    public static final byte[] CRLF = "\r\n".getBytes();

    public static void sendCommand(final OutputStream os, Pair<String, DataType> result) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Pair<String, DataType> handle(RedisInputStream in) {
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

    private static Pair<String, DataType> handleError(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    private static Pair<String, DataType> handleInteger(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    private static Pair<String, DataType> handleArray(RedisInputStream in) {
        int len = Integer.parseInt(in.readLine());
        List<String> args = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            byte _ = in.readByte();
            int elemLen = Integer.parseInt(in.readLine());
            args.add(in.readBytes(elemLen));
            in.ensureCrLf();
        }

        System.out.println("This is the elems in the array [");
        for (var elem : args) {
            System.out.println(elem);
        }
        System.out.println("]");

        if (!args.isEmpty()) {
            var cmd = Command.getCommand(args.get(0));

            if (Main.commandHandlers.containsKey(cmd)) {
                return Main.commandHandlers.get(cmd).handle(args.subList(1, args.size()));
            }
            throw new UnsupportedOperationException("This operation is not yet implemented.");
        }

        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    public static Pair<String, DataType> handle(RedisInputStream in, byte fb) {
        String req = (char) fb + in.readLine();

        Command cmd = Command.getCommand(req.substring(0, req.indexOf(' ')));

        var args = Arrays.stream(req.substring(req.indexOf(' ') + 1).split(" ")).toList();

        if (Main.commandHandlers.containsKey(cmd)) {
            return Main.commandHandlers.get(cmd).handle(args.subList(1, args.size()));
        }
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    private static Pair<String, DataType> handleBulkString(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    public static Pair<String, DataType> handleSimpleString(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }
}
