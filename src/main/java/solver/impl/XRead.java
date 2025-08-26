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

        ID lowest = Container.getLatestIdOfStream(stream);

        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            while (true) {
                if (lowest.compareTo(Container.getLatestIdOfStream(stream)) != 0) {
                    break;
                }
            }
        }

        if (!args.getLast().equals("$") && ID.parse(args.getLast()).compareTo(lowest) > 0) {
            lowest =  ID.parse(args.getLast());
        }

        boolean hasResult = false;
        List<String> res = new ArrayList<>();

        for (var key : Container.streamDirector.get(stream)) {
            System.out.println("************* " + key);
            if (lowest.compareTo(ID.parse(key)) >= 0) continue;
            hasResult = true;
            System.out.println(key);

            var props = Container.streamContainer.get(key);      // props
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
            sb.append(key.length());
            sb.append("\r\n");
            sb.append(key);
            sb.append("\r\n");
            sb.append(toRESP(allProps));
            res.add(sb.toString());
        }

        if (!hasResult) {
            return new Pair<>("-1", DataType.BULK_STRING);
        }

        if (args.getLast().equals("$")) {
            res = res.subList(res.size() - 1, res.size());
        }

        var sb = new StringBuilder();

        sb.append((char) DataType.ARRAYS.getSymbol());
        sb.append(res.size());
        sb.append("\r\n");
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

            for (var k : Container.streamDirector.get(stream)) {             // all keys belong to stream
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
        sb.append((char) DataType.ARRAYS.getSymbol());
        sb.append(args.size());
        sb.append("\r\n");
        for (var e: args) {
            sb.append((char) DataType.BULK_STRING.getSymbol());
            sb.append(e.length());
            sb.append("\r\n");
            sb.append(e);
            sb.append("\r\n");
        }
        return sb.toString();
    }
}