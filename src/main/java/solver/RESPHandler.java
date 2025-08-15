package solver;

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

    public static void sendCommand(final OutputStream os, String result) {
        try {
            int len = result.length();
            String builder = String.valueOf(DOLLAR_BYTE) +
                    len +
                    Arrays.toString(CRLF) +
                    result +
                    Arrays.toString(CRLF);
            os.write(builder.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
        For Errors the first byte of the reply is "-"
        For Integers the first byte of the reply is ":"
     */
    public static String handle(RedisInputStream in) {
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

    private static String handleError(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    private static String handleInteger(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    private static String handleArray(RedisInputStream in) {
        int len = Integer.parseInt(in.readLine());
        List<String> args = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            byte f = in.readByte();
            int elemLen = Integer.parseInt(in.readLine());
            args.add(in.readBytes(elemLen));
            in.ensureCrLf();
        }

        if (!args.isEmpty() && args.get(0).equals("ECHO")) {
            return args.get(1);
        } else if (!args.isEmpty() && args.get(0).equals("PING")) {
            return args.get(1);
        }

        return "";
    }

    private static String handleBulkString(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    public static String handleSimpleString(RedisInputStream in) {
        throw new UnsupportedOperationException("This operation is not yet implemented.");
    }

    public static String handle(RedisInputStream in, byte fb) {
        String req = (char)fb + in.readLine();

        String command = req.substring(0, req.indexOf(' '));

        if (Command.getCommand(command).equals(Command.ECHO)) {
            return command.substring(command.indexOf(' ') + 1);
        } else if (Command.getCommand(command).equals(Command.PING)) {
            return "PONG";
        }

        return "";
    }
}
