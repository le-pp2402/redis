package solver.impl;

import constants.DataType;
import constants.ID;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;

import java.util.ArrayList;
import java.util.List;

public class XRead implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        return !args.getFirst().equalsIgnoreCase("block") ? nonBlockingUsage(args) : blockingUsage(args);
    }

    private Pair<String, DataType> blockingUsage(List<String> args) {
        long waitTime = Long.parseLong(args.get(1));
        String stream = args.get(3);
        String lastArg = args.getLast();

        // For $, we want entries added AFTER this point
        ID lowest;
        if (lastArg.equals("$")) {
            lowest = Container.getLatestIdOfStream(stream);
        } else {
            lowest = ID.parse(lastArg);
        }

        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            // Block indefinitely until new entries arrive
            while (true) {
                if (lowest.compareTo(Container.getLatestIdOfStream(stream)) < 0) {
                    break;
                }
                try {
                    Thread.sleep(10); // Small sleep to prevent busy waiting
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        boolean hasResult = false;
        List<String> res = new ArrayList<>();

        // FIX: Use the new thread-safe method
        List<String> streamKeys = Container.getStreamKeys(stream);

        for (var key : streamKeys) {
            System.out.println("************* " + key);
            ID keyId = ID.parse(key);
            if (lowest.compareTo(keyId) >= 0) continue;
            hasResult = true;
            System.out.println(key);

            var props = Container.streamContainer.get(key);      // props
            if (props == null) continue; // Skip if properties don't exist

            var allProps = new ArrayList<String>();
            for (var elem : props.entrySet()) {
                allProps.add(elem.getKey());
                allProps.add(elem.getValue());
            }

            StringBuilder sb = new StringBuilder();
            sb.append("*2\r\n"); // Array with 2 elements (ID + fields)
            sb.append("$").append(key.length()).append("\r\n"); // Entry ID as bulk string
            sb.append(key).append("\r\n");
            sb.append(toRESP(allProps)); // Fields as array
            res.add(sb.toString());
        }

        if (!hasResult) {
            return new Pair<>("-1", DataType.BULK_STRING);
        }

        // For $, we should only return the latest entries, not filter by subList
        if (lastArg.equals("$") && !res.isEmpty()) {
            // Return all new entries that were added after the initial snapshot
            // Don't limit to just the last one
        }

        // Build the final response in correct RESP format
        var sb = new StringBuilder();
        sb.append("*1\r\n"); // Array with 1 element (the stream result)
        sb.append("*2\r\n"); // Array with 2 elements (stream name + entries)
        sb.append("$").append(stream.length()).append("\r\n"); // Stream name as bulk string
        sb.append(stream).append("\r\n");
        sb.append("*").append(res.size()).append("\r\n"); // Array of entries
        for (var elem : res) {
            sb.append(elem);
        }

        return new Pair<>(sb.toString(), DataType.ARRAYS);
    }

    private Pair<String, DataType> nonBlockingUsage (List < String > args) {
        var streams = new ArrayList<Pair<String, ID>>(); // stream and lowest id

        int shift = (args.size() - 1) / 2;

        for (int i = 1; i + shift < args.size(); i++) {
            streams.add(
                    new Pair<>(
                            args.get(i),
                            ID.parse(args.get(i + shift))
                    )
            );
        }

        List<String> eachStreams = new ArrayList<>();
        boolean hasResult = false;

        for (Pair<String, ID> key : streams) {
            var stream = key.first;
            var lowest = key.second;

            List<String> res = new ArrayList<>();

            // FIX: Use the new thread-safe method
            List<String> streamKeys = Container.getStreamKeys(stream);

            for (var k : streamKeys) {             // all keys belong to stream
                System.out.println("sdkfhasdkfhasfkhasf************* " + k);
                var id = ID.parse(k);

                if (lowest.compareTo(id) >= 0) continue;

                hasResult = true;

                var props = Container.streamContainer.get(id.toString());      // props
                var allProps = new ArrayList<String>();
                for (var elem : props.entrySet()) {
                    allProps.add(elem.getKey());
                    allProps.add(elem.getValue());
                }

                StringBuilder sb = new StringBuilder();
                sb.append((char) DataType.ARRAYS.getSymbol());
                sb.append(2);
                sb.append("\r\n");
                sb.append((char) DataType.BULK_STRING.getSymbol());
                sb.append(id.toString().length());
                sb.append("\r\n");
                sb.append(id.toString());
                sb.append("\r\n");
                sb.append(toRESP(allProps));
                res.add(sb.toString());
            }

            StringBuilder sb = new StringBuilder();
            sb.append((char) DataType.ARRAYS.getSymbol());
            sb.append(res.size());
            sb.append("\r\n");
            for (String s : res) {
                sb.append(s);
            }

            sb = new StringBuilder("*2\r\n")
                    .append((char) DataType.BULK_STRING.getSymbol())
                    .append(key.first.length())
                    .append("\r\n")
                    .append(key.first)
                    .append("\r\n")
                    .append(sb);

            eachStreams.add(sb.toString());
        }

        if (!hasResult) {
            return new Pair<>("-1", DataType.BULK_STRING);
        }

        StringBuilder sb = new StringBuilder();
        sb.append((char) DataType.ARRAYS.getSymbol());
        sb.append(eachStreams.size());
        sb.append("\r\n");

        for (String s : eachStreams) {
            sb.append(s);
        }

        return new Pair<>(sb.toString(), DataType.ARRAYS);
    }

    public String toRESP(List<String> args) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(args.size()).append("\r\n");
        for (var e: args) {
            sb.append("$").append(e.length()).append("\r\n");
            sb.append(e).append("\r\n");
        }
        return sb.toString();
    }
}