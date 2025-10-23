package constants;

public enum DataType {
    SIMPLE_STRING, ERROR, INTEGER, BULK_STRING, ARRAYS, STREAM, RDB;

    public byte getSymbol() {
        return switch (this) {
            case SIMPLE_STRING -> '+';
            case ERROR -> '-';
            case INTEGER -> ':';
            case BULK_STRING -> '$';
            case ARRAYS -> '*';
            case STREAM -> (byte) 0;
            case RDB -> (byte) 5; // ??
        };
    }
}
