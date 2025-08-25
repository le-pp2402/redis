package container;

import constants.DataType;
import constants.ID;
import solver.Pair;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Container {
    public static AtomicReference<ID> latestID = new AtomicReference<>(new ID(0, 0));
    public static ConcurrentHashMap<String, String> container = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, List<String>> streamDirector = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, String>> streamContainer = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long> lifetimeContainer = new ConcurrentHashMap<>();

    public static Pair<String, DataType> set(String key, String value, Long expiry) {
        if (expiry != null) {
            Long cur = System.currentTimeMillis();
            lifetimeContainer.put(key, cur + expiry);
        } else {
            lifetimeContainer.remove(key);
        }

        container.put(key, value);
        return new Pair<>("OK", DataType.SIMPLE_STRING);
    }

    public static Pair<String, DataType> get(String key) {
        Long cur = System.currentTimeMillis();

        if (lifetimeContainer.containsKey(key) && lifetimeContainer.get(key) < cur) {
            container.remove(key);
            lifetimeContainer.remove(key);
            return new Pair<>("-1", DataType.BULK_STRING);
        }

        if (container.containsKey(key)) {
            var value = container.get(key);
            if (streamContainer.containsKey(value)) {
                return new Pair<>("stream", DataType.STREAM);
            }
            return new Pair<>(container.get(key), DataType.BULK_STRING);
        } else {
            return new Pair<>("-1", DataType.SIMPLE_STRING );
        }
    }
}
