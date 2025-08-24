    package solver.impl;

    import constants.DataType;
    import constants.ID;
    import container.Container;
    import solver.ICommandHandler;
    import solver.Pair;

    import java.util.List;
    import java.util.concurrent.ConcurrentHashMap;

    public class XAdd implements ICommandHandler {

        @Override
        public Pair<String, DataType> handle(List<String> args) {
            String key = args.get(0);
            String value = args.get(1);

            var id = ID.parse(value);
            assert id != null;
            if (id.compareTo(new ID(0, 0)) == 0) {
                return new Pair<>("ERR The ID specified in XADD must be greater than 0-0", DataType.ERROR);
            }
            else if (id.compareTo(Container.latestID.get()) <= 0) {
                    return new Pair<>("ERR The ID specified in XADD is equal or smaller than the target stream top item", DataType.ERROR);
            }
            Container.latestID.set(id);

            ConcurrentHashMap<String, String> concurrentHashMap = new ConcurrentHashMap<>();
            for (int i = 2; i < args.size(); i += 2) {
                concurrentHashMap.put(args.get(i), args.get(i + 1));
            }

            Container.set(key, value, null);
            Container.streamContainer.put(value, concurrentHashMap);

            return new Pair<>(value, DataType.BULK_STRING);
        }
    }
