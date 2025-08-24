package solver.impl;

import constants.DataType;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class XAdd implements ICommandHandler {

    @Override
    public Pair<String, DataType> handle(List<String> args) {
        String key = args.get(0);
        String value = args.get(1);

        ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
        for (int i = 2; i < args.size(); i += 2) {
            concurrentHashMap.put(args.get(i), args.get(i + 1));
        }

        Container.set(key, value, null);
        Container.streamContainer.put(value, concurrentHashMap);

        return new Pair<>(value, DataType.BULK_STRING);
    }
}
