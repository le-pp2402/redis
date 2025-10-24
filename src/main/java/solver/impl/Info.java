package solver.impl;

import java.util.List;
import constants.DataType;
import solver.ICommandHandler;
import solver.Main;
import solver.Pair;

public class Info implements ICommandHandler {
    @Override
    public Pair<String, DataType> handle(List<String> args) {
        StringBuffer roleInfo = new StringBuffer("role:" + (Main.ROLE.toString().toLowerCase()));
        String replicationID = Main.replicationInfo.getReplicationID();
        String lastOffset = Long.toString(Main.replicationInfo.getOffset());
        roleInfo.append("\nmaster_replid:").append(replicationID);
        roleInfo.append("\nmaster_repl_offset:").append(lastOffset);
        return new Pair<>(roleInfo.toString(), DataType.BULK_STRING);
    }
}
