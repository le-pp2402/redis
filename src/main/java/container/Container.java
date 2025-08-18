package container;

import constants.DataType;
import solver.Pair;

import java.util.concurrent.ConcurrentHashMap;

public class Container {
    public static ConcurrentHashMap<String, String> container = new ConcurrentHashMap<>();

    public static Pair<String, DataType> set(String key, String value) {
        container.put(key, value);
        return new Pair<>("OK", DataType.SIMPLE_STRING);
    }

    public static Pair<String, DataType> get(String key) {
        if (container.containsKey(key)) {
            return new Pair<>(container.get(key), DataType.BULK_STRING);
        } else {
            return new Pair<>("-1", DataType.SIMPLE_STRING );
        }
    }
}
