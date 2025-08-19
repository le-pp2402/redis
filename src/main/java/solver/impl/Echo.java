package solver.impl;

import constants.DataType;
import solver.ICommandHandler;
import solver.Pair;

import java.util.List;

public class Echo implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        return new Pair<>(args.getFirst(), DataType.BULK_STRING);
    }
}
