package protocol;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class RespBuilder {
    private static final String CRLF = "\r\n";

    public static String simpleString(String str) {
        return "+" + str + CRLF;
    }

    public static String error(String errorCode, String str) {
        return "-" + errorCode.toUpperCase() + " " + str + CRLF;
    }

    public static String error(String message) {
        return "-ERR " + message + CRLF;
    }

    public static String integer(int num) {
        return ":" + num + CRLF;
    }

    public static String integer(long num) {
        return ":" + num + CRLF;
    }

    public static String bulkString(String str) {
        if (str == null)
            return "$-1" + CRLF;
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        return "$" + bytes.length + CRLF + str + CRLF;
    }

    public static String array(List<String> encodedItems) {
        if (encodedItems == null)
            return "*-1" + CRLF;
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(encodedItems.size()).append(CRLF);

        for (String item : encodedItems) {
            sb.append(item);
        }

        return sb.toString();
    }
}
