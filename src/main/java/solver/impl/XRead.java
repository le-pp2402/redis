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
        int start = 1;

        if (args.get(1).equals("block")) {
            start = 3;

            System.out.println("we need to wait " + args.get(1));
            var sleep = Long.parseLong(args.get(1));
            try {
                System.out.println("Start wait: " + System.currentTimeMillis());
                Thread.sleep(Math.max(0, sleep));
                System.out.println("End: " + System.currentTimeMillis());
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }

        int shift = (args.size() - start) / 2;
        List<Pair<String, ID>> keys = new ArrayList<>();

        for (int i = start; i + shift < args.size(); i++) {
            keys.add(
                    new Pair<>(
                            args.get(i),
                            ID.parse(args.get(i + shift))
                    )
            );
        }

        List<String> eachStreams = new ArrayList<>();

        for (Pair<String, ID> key : keys) {                                     // [stream_name] - [lowest_id]

            List<String> res = new ArrayList<>();

            for (var k : Container.streamDirector.get(key.first)) {             // all keys belong to [stream_name]
                var id = ID.parse(k);
                if (!inRange(key.second, id)) continue;

                var props = Container.streamContainer.get(id.toString());      // [key - 1], [key - 2]
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
                sb.append(k.length());
                sb.append("\r\n");
                sb.append(k);
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

        if (eachStreams.isEmpty()) {
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

    public boolean inRange(ID left, ID value) {
        return  left.compareTo(value) < 0;
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
