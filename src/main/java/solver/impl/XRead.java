package solver.impl;

import constants.CommonStatement;
import constants.DataType;
import constants.ID;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;
import solver.RESPHandler;
import utils.builds.RESPBuilder;
import utils.searching.BinarySearch;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class XRead implements ICommandHandler {

    private static final Logger logger = Logger.getLogger(XRead.class.getName());

    @Override
    public Pair<String, DataType> handle(List<String> args) {
        logger.info("\nXREAD receiving following args: \n");
        for (String arg : args) {
            logger.info(arg + " ");
        }

        if (args.get(0).equals(CommonStatement.block.toString())) {
            return blockedHandler(args.subList(1, args.size()));
        } else {
            return nonBlockedHandler(args.subList(1, args.size()));
        }
    }

    // Single stream
    private Pair<String, DataType> blockedHandler(List<String> args) {

        String stream = args.get(0), greaterId = args.get(1);

        var ids = Container.getStreamKeys(stream);
        var id_pos = BinarySearch.upperBound(ids, ID.parse(greaterId));

        if (id_pos == ids.size()) {
            return new Pair<String, DataType>(null, DataType.BULK_STRING);
        } else {
            var result = buildRESP(ids.subList(id_pos, ids.size()));
            StringBuffer sb = new StringBuffer();
            sb.append((char) DataType.ARRAYS.getSymbol());
            sb.append(2);
            sb.append(RESPHandler.CRLF);
            sb.append(RESPBuilder.buildBulkString(stream));
            sb.append(RESPBuilder.buildArray(result));
            return new Pair<>(sb.toString(), DataType.ARRAYS);
        }
    }

    private Pair<String, DataType> nonBlockedHandler(List<String> args) {
        List<String> streams = new ArrayList<>();
        List<String> greaterIds = new ArrayList<>();
        int skip = args.size() / 2;
        for (int i = 0; i < skip; i++) {
            streams.add(args.get(i));
            greaterIds.add(args.get(i + skip));
        }

        List<StringBuffer> res = new ArrayList<>();

        for (int ind = 0; ind < streams.size(); ind++) { // for each stream
            String stream = streams.get(ind);
            String greaterId = greaterIds.get(ind);

            var ids = Container.getStreamKeys(stream);
            var id_pos = BinarySearch.upperBound(ids, ID.parse(greaterId));

            if (id_pos < ids.size()) { // get elem from stream
                var result = buildRESP(ids.subList(id_pos, ids.size()));
                StringBuffer sb = new StringBuffer();
                sb.append((char) DataType.ARRAYS.getSymbol());
                sb.append(2);
                logger.info("XREAD adding stream " + sb.toString() + " to response.");
                sb.append(RESPBuilder.CRLF);
                sb.append(RESPBuilder.buildBulkString(stream));
                sb.append(RESPBuilder.buildArray(result));
                res.add(sb);
            }
        }

        if (res.isEmpty()) {
            return new Pair<>(null, DataType.BULK_STRING);
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append((char) DataType.ARRAYS.getSymbol());
            sb.append(res.size());
            sb.append(RESPBuilder.CRLF);

            logger.info("XREAD cur: " + sb.toString());

            for (var e : res) {
                sb.append(e.toString());
            }

            logger.info("XREAD response: \n" + sb.toString());

            return new Pair<>(sb.toString(), DataType.ARRAYS);
        }
    }

    private List<StringBuffer> buildRESP(List<ID> ids) {
        var result = new ArrayList<StringBuffer>();

        for (ID id : ids) {
            StringBuffer sb = new StringBuffer();
            sb.append((char) DataType.ARRAYS.getSymbol());
            sb.append(2);
            sb.append(RESPBuilder.CRLF);
            sb.append(RESPBuilder.buildBulkString(id.toString()));
            sb.append((char) DataType.ARRAYS.getSymbol());
            var allKV = Container.streamContainer.get(id);
            sb.append(allKV.size() * 2);
            sb.append(RESPBuilder.CRLF);
            for (var elem : allKV.entrySet()) {
                sb.append(RESPBuilder.buildBulkString(elem.getKey()));
                sb.append(RESPBuilder.buildBulkString(elem.getValue()));
            }
            result.add(sb);
        }

        return result;
    }
}