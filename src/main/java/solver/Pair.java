package solver;

import constants.DataType;

public class Pair<T, G> {
    public T first;
    public G second;

    public Pair() {}

    public Pair(T first, G second) {
        this.first = first;
        this.second = second;
    }

    public static Pair<String, DataType> getNull() {
        return new Pair<>("-1", DataType.SIMPLE_STRING);
    }
}
