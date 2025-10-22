package solver.impl;

import java.util.List;

import org.apache.log4j.Logger;

import constants.DataType;
import container.Container;
import solver.ICommandHandler;
import solver.Pair;

public class Incr implements ICommandHandler {
    private static final Logger logger = Logger.getLogger(Incr.class.getName());

    @Override
    public Pair<String, DataType> handle(List<String> args) {
        var key = args.get(0);

        if (Container.container.get(key) == null) {
            Container.container.put(key, "1");
            return new Pair<>("1", DataType.INTEGER);
        }

        var value = Container.get(key);

        logger.info("INCR called on key: " + key + " with current value: " + value.first);

        try {
            var num = Long.parseLong(value.first) + 1;
            logger.info("num = " + num + "value of num = " + String.valueOf(num));
            Container.container.put(key, String.valueOf(num));
            return new Pair<>(String.valueOf(num), DataType.INTEGER);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return null;
    }

}
