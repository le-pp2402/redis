package solver.impl;

import constants.DataType;
import constants.ID;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class XRange implements ICommandHandler {
    private static final Logger logger = Logger.getLogger(XRange.class.getName());

    @Override
    public Pair<String, DataType> handle(List<String> args) {
        List<String> res = new ArrayList<>();

        if (args.get(1).equals("-")) {
            args.set(1, "0-0");
        }

        if (args.get(2).equals("+")) {
            long cur = System.currentTimeMillis();
            args.set(2, Long.toString(cur + 10) + "-0");
        }

        ID left = ID.parse(args.get(1));
        ID right = ID.parse(args.get(2));
        assert left != null && right != null;

        Container.streamContainer.forEach((id, props) -> {
            if (!inRange(left, right, id))
                return;

            logger.info("Checking ID " + id + " in range " + left + " to " + right);

            var allProps = new ArrayList<String>();

            for (var elem : props.entrySet()) {
                String prop = elem.getKey();
                System.out.println(prop);
                System.out.println(elem.getValue());

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
            sb.append(id);
            sb.append("\r\n");
            sb.append(toRESP(allProps));
            res.add(sb.toString());
        });

        StringBuilder sb = new StringBuilder();
        sb.append((char) DataType.ARRAYS.getSymbol());
        sb.append(res.size());
        sb.append("\r\n");

        res.sort(String::compareTo);

        for (var e : res) {
            sb.append(e);
            System.out.println(e);
        }

        return new Pair<>(sb.toString(), DataType.ARRAYS);
    }

    public boolean inRange(ID left, ID right, ID value) {
        return (left.compareTo(value) <= 0 && right.compareTo(value) >= 0);
    }

    public String toRESP(List<String> args) {
        StringBuilder sb = new StringBuilder();
        sb.append((char) DataType.ARRAYS.getSymbol());
        sb.append(args.size());
        sb.append("\r\n");
        for (var e : args) {
            sb.append((char) DataType.BULK_STRING.getSymbol());
            sb.append(e.length());
            sb.append("\r\n");
            sb.append(e);
            sb.append("\r\n");
        }
        return sb.toString();
    }
}
