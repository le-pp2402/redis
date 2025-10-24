package solver.impl;

import java.util.List;
import org.apache.log4j.Logger;
import constants.DataType;
import solver.ICommandHandler;
import solver.Pair;

public class Replconf implements ICommandHandler {
    private static final Logger log = Logger.getLogger(Replconf.class);

    @Override
    public Pair<String, DataType> handle(List<String> args) {
        log.info("REPLCONF command received with args: " + args);
        // for (String arg : args) {
        // if (arg.equalsIgnoreCase("listening-port")) {
        // int port = Integer.parseInt(args.get(args.indexOf(arg) + 1));
        // }
        // }
        return new Pair<>("OK", DataType.SIMPLE_STRING);
    }

}
