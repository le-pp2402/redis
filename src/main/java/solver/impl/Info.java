package solver.impl;

import java.util.List;
import constants.DataType;
import solver.ICommandHandler;
import solver.Main;
import solver.Pair;

public class Info implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        String roleInfo = "role:" + (Main.ROLE.toString().toLowerCase());
        return new Pair<>(roleInfo, DataType.BULK_STRING);
    }
}
