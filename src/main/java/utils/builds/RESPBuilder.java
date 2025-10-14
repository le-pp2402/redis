package utils.builds;

import java.util.List;

import constants.DataType;
import solver.RESPHandler;

public class RESPBuilder {
    public static StringBuffer buildBulkString(String str) {
        StringBuffer sb = new StringBuffer();
        sb.append(DataType.BULK_STRING.getSymbol());
        sb.append(str.length());
        sb.append(RESPHandler.CRLF);
        sb.append(str);
        sb.append(RESPHandler.CRLF);
        return sb;
    }

    public static StringBuffer buildArray(List<StringBuffer> str) {
        StringBuffer sb = new StringBuffer();
        sb.append(DataType.ARRAYS.getSymbol());
        sb.append(str.size());
        sb.append(RESPHandler.CRLF);
        for (var strbuff : str) {
            sb.append(strbuff);
        }
        return sb;
    }
}
