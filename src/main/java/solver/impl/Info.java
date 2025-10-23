package solver.impl;

import java.util.List;

import constants.DataType;
import solver.ICommandHandler;
import solver.Pair;

public class Info implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        return new Pair<>("role:master", DataType.BULK_STRING);
    }
}
