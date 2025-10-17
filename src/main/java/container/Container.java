package container;

import constants.DataType;
import constants.ID;
import solver.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

public class Container {
    private static final Logger logger = Logger.getLogger(Container.class.getName());

    public static AtomicReference<ID> latestID = new AtomicReference<>(new ID(0, 0));
    public static ConcurrentHashMap<String, ID> lastestIdOfStream = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, String> container = new ConcurrentHashMap<>();

    // name of stream -> key of object
    public static ConcurrentHashMap<String, List<ID>> streamDirector = new ConcurrentHashMap<>();

    // key of object -> object
    public static ConcurrentHashMap<ID, ConcurrentHashMap<String, String>> streamContainer = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, Long> lifetimeContainer = new ConcurrentHashMap<>();

    public static ID getLatestIdOfStream(String stream) {
        if (lastestIdOfStream.containsKey(stream)) {
            return lastestIdOfStream.get(stream);
        } else {
            return new ID(0, 0);
        }
    }

    public static void setLatestIdOfStream(String stream, ID newID) {
        lastestIdOfStream.put(stream, newID);
    }

    public static void addStreamKey(String stream, ID key) {
        logger.info("Adding " + key + " to stream " + stream);
        streamDirector.computeIfAbsent(stream, _ -> new ArrayList<>()).add(key);
    }

    public static List<ID> getStreamKeys(String stream) {
        return streamDirector.getOrDefault(stream, new CopyOnWriteArrayList<>());
    }

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
            if (streamDirector.containsKey(key)) {
                return new Pair<>("stream", DataType.STREAM);
            }
            return new Pair<>(container.get(key), DataType.BULK_STRING);
        } else {
            return new Pair<>("-1", DataType.BULK_STRING);
        }
    }
}