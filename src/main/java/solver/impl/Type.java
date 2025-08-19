package solver.impl;

import constants.DataType;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;

import java.util.List;

public class Type implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        if (Container.get(args.getFirst()).equals(Pair.getNull())) {
            return new Pair<>("none", DataType.SIMPLE_STRING);
        }
        return new Pair<>("string", DataType.SIMPLE_STRING);
    }
}
