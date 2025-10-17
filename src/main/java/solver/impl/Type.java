package solver.impl;

import constants.DataType;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;

import java.util.List;

import org.apache.log4j.Logger;

public class Type implements ICommandHandler {
    public static final Logger logger = Logger.getLogger(Type.class.getName());

    @Override
    public Pair<String, DataType> handle(List<String> args) {

        logger.info("Handling TYPE command " + args.toString());
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
