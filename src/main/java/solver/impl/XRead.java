package solver.impl;

import constants.CommonStatement;
import constants.DataType;
import constants.ID;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;
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

        // block 0 stream
        if (args.get(0).equalsIgnoreCase(CommonStatement.block.toString())) {
            return blockedHandler(args.subList(1, args.size()));
        } else {
            return nonBlockedHandler(args.subList(1, args.size()));
        }
    }

    // Single stream
    private Pair<String, DataType> blockedHandler(List<String> args) {

        for (String arg : args) {
            logger.info("XREAD BLOCK arg: " + arg + " ");
        }

        // 0 stream
        int timeout = Integer.parseInt(args.get(0));

        List<String> streams = new ArrayList<>();
        List<ID> greaterIds = new ArrayList<>();
        int skip = (args.size() - 2) / 2;

        for (int i = 2; i + skip < args.size(); i++) {
            var stream = args.get(i);
            streams.add(stream);
            var curId = Container.getLatestIdOfStream(stream);

            if (args.get(i + skip).equalsIgnoreCase(CommonStatement.$.toString())) {
                greaterIds.add(curId);
                continue;
            }

            if (curId.compareTo(ID.parse(args.get(i + skip))) > 0) {
                greaterIds.add(curId);
            } else {
                greaterIds.add(ID.parse(args.get(i + skip)));
            }
        }

        if (timeout != 0) {
            logger.info("Start waiting at " + System.currentTimeMillis());
            List<StringBuffer> res = new ArrayList<>();
            try {
                Thread.sleep(Math.max(timeout, 0L));
                logger.info("XREAD BLOCK woke up from sleep. Current time: " + System.currentTimeMillis());
                for (int ind = 0; ind < streams.size(); ind++) { // for each stream
                    String stream = streams.get(ind);
                    ID greaterId = greaterIds.get(ind);

                    var ids = Container.getStreamKeys(stream);
                    var id_pos = BinarySearch.upperBound(ids, greaterId);

                    if (id_pos < ids.size()) {
                        var result = buildRESP(ids.subList(id_pos, ids.size()));
                        StringBuffer sb = new StringBuffer();
                        sb.append((char) DataType.ARRAYS.getSymbol());
                        sb.append(2);
                        sb.append(RESPBuilder.CRLF);
                        sb.append(RESPBuilder.buildBulkString(stream));
                        sb.append(RESPBuilder.buildArray(result));
                        res.add(sb);
                    }
                }
            } catch (Exception e) {
                logger.error("XREAD BLOCK exception: " + e.getMessage());
            }

            if (res.isEmpty()) {
                return new Pair<>(null, DataType.ARRAYS);
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append((char) DataType.ARRAYS.getSymbol());
                sb.append(res.size());
                sb.append(RESPBuilder.CRLF);

                logger.info("XREAD BLOCK cur: " + sb.toString());

                for (var e : res) {
                    sb.append(e.toString());
                }

                logger.info("XREAD BLOCK response: \n" + sb.toString());

                return new Pair<>(sb.toString(), DataType.ARRAYS);
            }
        } else {
            try {
                while (true) {
                    Thread.sleep(50);
                    for (int ind = 0; ind < streams.size(); ind++) {
                        String stream = streams.get(ind);
                        ID greaterId = greaterIds.get(ind);

                        var ids = Container.getStreamKeys(stream).getLast();
                        if (ids.compareTo(greaterId) > 0) {
                            var result = buildRESP(List.of(ids));
                            StringBuffer sb = new StringBuffer();
                            sb.append((char) DataType.ARRAYS.getSymbol());
                            sb.append(2);
                            sb.append(RESPBuilder.CRLF);
                            sb.append(RESPBuilder.buildBulkString(stream));
                            sb.append(RESPBuilder.buildArray(result));

                            // var res = RESPBuilder.buildArray(List.of(sb));

                            StringBuffer res = new StringBuffer();
                            res.append((char) DataType.ARRAYS.getSymbol());
                            res.append(1);
                            res.append(RESPBuilder.CRLF);
                            res.append(sb.toString());

                            return new Pair<>(res.toString(), DataType.ARRAYS);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("XREAD BLOCK exception: " + e.getMessage());
            }
        }

        assert (false);

        return null;
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