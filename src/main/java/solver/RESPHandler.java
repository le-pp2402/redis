package solver;

import constants.Argument;
import constants.DataType;
import container.Container;
import utils.RedisInputStream;
import constants.Command;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
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
            int len = result.first.length();
            StringBuilder sb = new StringBuilder();

            sb.append((char)result.second.getSymbol());

            if (result.second != DataType.SIMPLE_STRING)
                if (!(result.second == DataType.BULK_STRING && result.first.equals("-1"))) {
                    sb.append(result.first.length());
                    sb.append("\r\n");
                }

            sb.append(result.first);
            sb.append("\r\n");

            os.write(sb.toString().getBytes());
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
            byte f = in.readByte();
            int elemLen = Integer.parseInt(in.readLine());
            args.add(in.readBytes(elemLen));
            in.ensureCrLf();
        }

        if (!args.isEmpty() && args.get(0).equals(Command.ECHO.toString())) {
            return new Pair<> (args.get(1), DataType.BULK_STRING);
        } else if (!args.isEmpty() && args.get(0).equals(Command.PING.toString())) {
            return new Pair<>("PONG", DataType.SIMPLE_STRING);
        } else if (!args.isEmpty() && args.get(0).equals(Command.GET.toString())) {
            return Container.get(args.get(1));
        } else if (!args.isEmpty() && args.get(0).equals(Command.SET.toString())) {
            Long expr = null;

            if (args.size() > 2 && Argument.getArgument(args.get(2)).equals(Argument.PX)) {
                expr = Long.parseLong(args.get(3));
            }
            return Container.set(args.get(1), args.get(2),  expr);
        }

        throw  new UnsupportedOperationException("This operation is not yet implemented.");
    }

    private static Pair<String, DataType> handleBulkString(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    public static Pair<String, DataType> handleSimpleString(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    public static Pair<String, DataType> handle(RedisInputStream in, byte fb) {
        String req = (char)fb + in.readLine();

        String command = req.substring(0, req.indexOf(' '));

        String[] args = req.substring(req.indexOf(' ') + 1).split(" ");

        if (Command.getCommand(command).equals(Command.ECHO)) {
            return new Pair<> (req.substring(req.indexOf(' ') + 1), DataType.BULK_STRING);
        } else if (Command.getCommand(command).equals(Command.PING)) {
            return new Pair<>("PONG", DataType.SIMPLE_STRING);
        } else if (Command.getCommand(command).equals(Command.GET)) {
            return new Pair<>(req.substring(req.indexOf(' ') + 1), DataType.BULK_STRING);
        } else if (Command.getCommand(command).equals(Command.SET)) {
            return new Pair<>(req.substring(req.indexOf(' ') + 1), DataType.SIMPLE_STRING);
        } else if (Command.getCommand(command).equals(Command.SET)) {
            return Container.get(args[1]);
        } else if (Command.getCommand(command).equals(Command.SET)) {
            Long expr = null;
            if (args.length > 2 && Argument.getArgument(args[2]).equals(Argument.PX)) {
                expr = Long.parseLong(args[3]);
            }
            return Container.set(args[1], args[2],  expr);
        }
        throw  new UnsupportedOperationException("This operation is not yet implemented.");
    }
}
