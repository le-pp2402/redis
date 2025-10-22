package constants;

public enum Command {
    PING, SET, GET, ECHO, TYPE, XADD, XRANGE, XREAD, INCR;

    public static Command getCommand(String command) {
        String upperCommand = command.toUpperCase();
        for (Command c : Command.values()) {
            if (c.name().equals(upperCommand)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown command: " + command);
    }
}
