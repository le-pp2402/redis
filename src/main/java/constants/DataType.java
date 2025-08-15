package constants;

public enum DataType {
    SIMPLE_STRING, ERROR, INTEGER, BULK_STRING, ARRAYS;

    public byte getSymbol() {
        return switch (this) {
            case SIMPLE_STRING -> '+';
            case ERROR -> '-';
            case INTEGER -> ':';
            case BULK_STRING -> '$';
            case ARRAYS -> '*';
        };
    }
}
