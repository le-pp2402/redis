package solver.impl;

import constants.DataType;
import solver.ICommandHandler;
import solver.Pair;

import java.util.List;

public class Echo implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        return new Pair<>(args.get(0), DataType.BULK_STRING);
    }
}
