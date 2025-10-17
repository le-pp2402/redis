package utils.builds;

import java.util.List;

import org.apache.log4j.Logger;

import constants.DataType;

public class RESPBuilder {
    private static final Logger logger = Logger.getLogger(RESPBuilder.class.getName());

    public static char[] CRLF = { '\r', '\n' };

    public static StringBuffer buildBulkString(String str) {
        StringBuffer sb = new StringBuffer();
        sb.append((char) DataType.BULK_STRING.getSymbol());
        sb.append(str.length());
        sb.append(CRLF);
        logger.info("in build BulkString: " + sb);
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
