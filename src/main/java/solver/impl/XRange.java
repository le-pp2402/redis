package solver.impl;

import constants.DataType;
import constants.ID;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;
import java.util.ArrayList;
import java.util.List;

public class XRange implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        List<String> res = new ArrayList<>();

        System.out.println("********************");;
        for (var x: Container.streamContainer.entrySet()) {
            System.out.println(x.getKey());
        }
        System.out.println("********************");;

        for (var e: Container.streamContainer.entrySet()) {
            ID id = ID.parse(e.getKey());
            ID left = ID.parse(args.get(1));
            ID right = ID.parse(args.get(2));

            assert left != null && right != null && id != null;
            if (!inRange(left, right, id)) continue;

            var props = Container.streamContainer.get(id.toString());
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
            sb.append(e.getKey().length());
            sb.append("\r\n");
            sb.append(e.getKey());
            sb.append("\r\n");

            var db = toRESP(allProps);

            sb.append(toRESP(allProps));
            res.add(sb.toString());
        }

        StringBuilder sb = new StringBuilder();
        sb.append((char) DataType.ARRAYS.getSymbol());
        sb.append(res.size());
        sb.append("\r\n");
        for (var e : res) {
            sb.append(e);
            System.out.println(e);
        }

        return new Pair<>(sb.toString(), DataType.ARRAYS);
    }

    public boolean inRange(ID left, ID right, ID value) {
        return  (left.compareTo(value) <= 0 && right.compareTo(value) >= 0);
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
