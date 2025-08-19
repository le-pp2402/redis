package solver;

import constants.DataType;

import java.util.List;

public interface ICommandHandler {
    Pair<String, DataType> handle(List<String> args);
}
