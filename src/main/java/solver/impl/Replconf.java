package solver.impl;

import java.util.List;

import constants.DataType;
import solver.ICommandHandler;
import solver.Main;
import solver.Pair;

public class Replconf implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        if (args.size() < 2 && args.get(1).toUpperCase().contains("PORT")) {
            // TODO: potential issue if the third step is failed, then the port is still
            // added
            Main.slavePorts.add(Integer.parseInt(args.get(2)));
        }
        return new Pair<>("OK", DataType.SIMPLE_STRING);
    }

}
