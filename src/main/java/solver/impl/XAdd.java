package solver.impl;

import com.sun.source.tree.ContinueTree;
import constants.DataType;
import constants.ID;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class XAdd implements ICommandHandler {

    @Override
    public Pair<String, DataType> handle(List<String> args) {
        var key = args.get(0);
        var value = args.get(1);

        /*
        When * is used for the sequence number, Redis picks the last sequence number used in the stream (for the same time part) and increments it by 1.
        The default sequence number is 0. The only exception is when the time part is also 0. In that case, the default sequence number is 1.
         */
        var id = autoGenerateID(value);

        assert id != null;

        if (id.compareTo(new ID(0, 0)) == 0) {
            return new Pair<>("ERR The ID specified in XADD must be greater than 0-0", DataType.ERROR);
        }
        else if (id.compareTo(Container.latestID.get()) <= 0) {
                return new Pair<>("ERR The ID specified in XADD is equal or smaller than the target stream top item", DataType.ERROR);
        }
        Container.latestID.set(id);

        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        for (int i = 2; i < args.size(); i += 2) {
            concurrentHashMap.put(args.get(i), args.get(i + 1));
        }

        System.out.println(key);

        System.out.println("****************\n");
        for (var elem: Container.streamDirector.entrySet()) {
            System.out.println(elem.getKey());
            System.out.println(elem.getValue());
            System.out.println("--");
        }
        System.out.println("****************\n");

        if (Container.streamDirector.contains(key)) {
            System.out.println("[BEFORE] All key");
            var curKeys = Container.streamDirector.get(key);
            for (var c: curKeys) {
                System.out.println(c);
            }

            curKeys.add(id.toString());
            System.out.println("[AFTER] All key");
            for (var c: curKeys) {
                System.out.println(c);
            }
            Container.streamDirector.replace(key, curKeys);
        } else {
            var keys = new ArrayList<String>();
            keys.add(id.toString());
            System.out.println("[AFTER] All key");
            for (var c: keys) {
                System.out.println(c);
            }
            Container.streamDirector.put(key, keys);
        }

        Container.set(key, id.toString(), null);
        Container.streamContainer.put(id.toString(), concurrentHashMap);

        return new Pair<>(id.toString(), DataType.BULK_STRING);
    }

    public ID autoGenerateID(String id) {
        if (id.equals("*")) {
            var primaryPart = System.currentTimeMillis();
            var seqPart = 0;
            return new ID(primaryPart, seqPart);
        } else if (id.contains("*")) {
            var curKey = Container.latestID.get().milliseconds;
            if (Integer.parseInt(id.substring(0, id.indexOf('-'))) == curKey) {
                id = id.substring(0, id.length() - 1);
                id += String.valueOf(Container.latestID.get().sequenceNumber + 1);
            } else {
                id = id.replace('*', '0');
            }
        }
        return ID.parse(id);
    }
}
