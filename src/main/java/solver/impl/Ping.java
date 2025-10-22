package solver.impl;

import constants.DataType;
import solver.ICommandHandler;
import solver.Pair;

import java.util.List;

public class Ping implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        return new Pair<>("PONG", DataType.SIMPLE_STRING);
    }
}
