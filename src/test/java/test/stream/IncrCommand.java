package test.stream;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import test.BaseTest;

public class IncrCommand {
    // Test for INCR command

    private static final Logger logger = Logger.getLogger(IncrCommand.class.getName());

    @Test
    public void Test() {
        String[] input = {
                "\r\n$3\r\nSET\r\n$5\r\napple\r\n$2\r\n33\r\n",
                "*2\r\n$4\r\nINCR\r\n$5\r\napple\r\n"
        };

        String[] expectedOutput = {
                "+OK\r\n",
                ":34\r\n"
        };

        try {
            BaseTest.forDebug(List.of(input));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
