package solver.impl;

import constants.DataType;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;

import java.util.List;

public class Get implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        return Container.get(args.getFirst());
    }
}
