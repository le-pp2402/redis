package test.stream;

import org.junit.jupiter.api.Test;

import test.BaseTest;

public class XReadCommand {

    /*
     * XADD pineapple 0-1 temperature 89
     * XREAD streams pineapple 0-0
     */
    @Test
    public void TestSingleStream() {
        String[] input = {
                "*5\r\n$4\r\nXADD\r\n$9\r\npineapple\r\n$3\r\n0-1\r\n$11\r\ntemperature\r\n$2\r\n89\r\n",
                "*4\r\n$5\r\nXREAD\r\n$7\r\nstreams\r\n$9\r\npineapple\r\n$3\r\n0-0\r\n",
        };

        try {
            // BaseTest.forDebug(java.util.List.of(input));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
