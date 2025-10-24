// package constants.replication;

// import java.io.IOException;
// import java.util.List;
// import org.apache.log4j.Logger;

// import solver.Main;

// public class PropagateHandler extends Thread {
// private final static Logger log = Logger.getLogger(PropagateHandler.class);
// private List<String> args;

// public PropagateHandler(List<String> args) {
// this.args = args;
// }

// @Override
// public void run() {
// for (var out : Main.slaves) {
// try {
// for (String arg : args) {
// log.info("Sending to slave: " + arg);
// out.write(arg.getBytes());
// }
// } catch (IOException e) {
// log.error("Failed to propagate command to slave: " + e.getMessage());
// }
// }
// }
// }
