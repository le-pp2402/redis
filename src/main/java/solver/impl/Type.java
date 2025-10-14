package solver.impl;

import constants.DataType;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;

import java.util.List;

public class Type implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        var db = Container.get(args.get(0));

        if (db.first.equals("-1") && db.second.equals(DataType.BULK_STRING)) {
            return new Pair<>("none", DataType.SIMPLE_STRING);
        }

        if (db.second.equals(DataType.STREAM)) {
            return new Pair<>("stream", DataType.SIMPLE_STRING);
        }

        return new Pair<>("string", DataType.SIMPLE_STRING);
    }
}
