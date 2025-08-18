package constants;

public enum Argument {
    PX;
    public static Argument getArgument(String arg) {
        String argument = arg.toUpperCase();
        for (Argument c : Argument.values()) {
            if (c.name().equals(argument)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown argument: " + arg);
    }
}
