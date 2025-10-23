package solver.impl;

import java.util.List;

import constants.DataType;
import solver.ICommandHandler;
import solver.Pair;

public class Replconf implements ICommandHandler {

    @Override
    public Pair<String, DataType> handle(List<String> args) {
        return new Pair<>("OK", DataType.SIMPLE_STRING);
    }

}
