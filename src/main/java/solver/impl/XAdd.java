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
            var key = args.get(0);
            var value = args.get(1);

            /*
            When * is used for the sequence number, Redis picks the last sequence number used in the stream (for the same time part) and increments it by 1.
            The default sequence number is 0. The only exception is when the time part is also 0. In that case, the default sequence number is 1.
             */

            if (value.charAt(value.length() - 1) == '*') {
                var curKey = Container.latestID.get().sequenceNumber;
                if (Integer.parseInt(key) == curKey) {
                    value = value.substring(0, value.length() - 1);
                    value += String.valueOf(curKey + 1);
                } else {
                    value = value.replace('*', '0');
                }
            }

            System.out.println(value);

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
