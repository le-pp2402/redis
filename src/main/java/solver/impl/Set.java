package solver.impl;

import constants.Argument;
import constants.DataType;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;

import java.util.List;

public class Set implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        Long expr = null;
        if (args.size() > 2 && Argument.getArgument(args.get(2)).equals(Argument.PX)) {
            expr = Long.parseLong(args.get(3));
        }
        return Container.set(args.get(0), args.get(1), expr);
    }
}
