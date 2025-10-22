package utils.builds;

import java.util.List;

import constants.DataType;

public class RESPBuilder {
    public static char[] CRLF = { '\r', '\n' };

    public static StringBuffer buildBulkString(String str) {
        StringBuffer sb = new StringBuffer();
        sb.append((char) DataType.BULK_STRING.getSymbol());
        sb.append(str.length());
        sb.append(CRLF);
        sb.append(str);
        sb.append(CRLF);
        return sb;
    }

    public static StringBuffer buildArray(List<StringBuffer> str) {
        StringBuffer sb = new StringBuffer();
        sb.append((char) DataType.ARRAYS.getSymbol());
        sb.append(str.size());
        sb.append(CRLF);
        for (var strbuff : str) {
            sb.append(strbuff);
        }
        return sb;
    }
}
