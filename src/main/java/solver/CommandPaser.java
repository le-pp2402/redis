package solver;

import constants.Command;

public class CommandPaser {

    public static boolean IsValidCommand(String in) {
        String[] commands = in.split(" ");
        try {
            Command cm = Command.getCommand(commands[0]);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
