package solver.impl;

import constants.DataType;
import constants.ID;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import java.util.List;

public class XAdd implements ICommandHandler {
    public static final Logger logger = Logger.getLogger(XAdd.class.getName());

    private final static int INDEX_OF_STREAM_NAME = 0;
    private final static int INDEX_OF_ID = 1;
    private final static int INDEX_OF_OBJECT = 2;

    @Override
    public Pair<String, DataType> handle(List<String> args) {
        var stream = args.get(INDEX_OF_STREAM_NAME);
        var value = args.get(INDEX_OF_ID);

        /*
         * When * is used for the sequence number, Redis picks the last sequence number
         * used in the stream (for the same time part) and increments it by 1.
         * The default sequence number is 0. The only exception is when the time part is
         * also 0. In that case, the default sequence number is 1.
         */
        var id = autoGenerateID(stream, value);

        assert id != null;

        if (id.compareTo(new ID(0, 0)) == 0) {
            return new Pair<>("ERR The ID specified in XADD must be greater than 0-0", DataType.ERROR);
        } else if (id.compareTo(Container.getLatestIdOfStream(stream)) <= 0) {
            return new Pair<>("ERR The ID specified in XADD is equal or smaller than the target stream top item",
                    DataType.ERROR);
        }

        Container.setLatestIdOfStream(stream, id);

        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        for (int i = INDEX_OF_OBJECT; i < args.size(); i += 2) {
            var key = args.get(i);
            var val = args.get(i + 1);
            concurrentHashMap.put(key, val);
        }

        logger.info("\nAdding to stream " + stream);

        System.out.println("****************\n");
        for (var elem : Container.streamDirector.entrySet()) {
            System.out.println(elem.getKey());
            System.out.println(elem.getValue());
            System.out.println("--");
        }
        System.out.println("****************\n");

        Container.addStreamKey(stream, id);
        Container.set(stream, id.toString(), null);
        Container.streamContainer.put(id, concurrentHashMap);

        return new Pair<>(id.toString(), DataType.BULK_STRING);
    }

    public ID autoGenerateID(String stream, String id) {
        if (id.equals("*")) {
            var primaryPart = System.currentTimeMillis();
            var seqPart = 0;
            return new ID(primaryPart, seqPart);
        } else if (id.contains("*")) {
            var curKey = Container.getLatestIdOfStream(stream);

            if (Integer.parseInt(id.substring(0, id.indexOf('-'))) == curKey.milliseconds) {
                id = id.substring(0, id.length() - 1);
                id += String.valueOf(curKey.sequenceNumber + 1);
            } else {
                id = id.replace('*', '0');
            }
        }
        return ID.parse(id);
    }
}