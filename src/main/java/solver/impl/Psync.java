package solver.impl;

import java.util.List;

import constants.DataType;
import solver.ICommandHandler;
import solver.Main;
import solver.Pair;

public class Psync implements ICommandHandler {

    @Override
    public Pair<String, DataType> handle(List<String> args) {
        String response = "FULLRESYNC " + Main.replicationInfo.getReplicationID() + " 0";
        return new Pair<>(response, DataType.SIMPLE_STRING);
    }
}
