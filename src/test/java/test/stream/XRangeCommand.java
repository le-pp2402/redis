package test.stream;

import java.util.List;
import org.junit.jupiter.api.Test;
import test.BaseTest;

public class XRangeCommand {

    /*
     * send:
     * XADD grape 0-1 strawberry pear
     * XADD grape 0-2 banana raspberry
     * XADD grape 0-3 strawberry pineapple
     * XRANGE grape 0-2 0-3
     * 
     * receive:
     * "0-1"
     * "0-2"
     * "0-3"
     * 1) 1) "0-2"
     * 2) 1) "banana"
     * 2) "raspberry"
     * 2) 1) "0-3"
     * 2) 1) "strawberry"
     * 2) "pineapple"
     */
    @Test
    public void Test() {

        String[] input = {
                "*5\r\n$4\r\nXADD\r\n$5\r\ngrape\r\n$3\r\n0-1\r\n$10\r\nstrawberry\r\n$4\r\npear\r\n",
                "*5\r\n$4\r\nXADD\r\n$5\r\ngrape\r\n$3\r\n0-2\r\n$6\r\nbanana\r\n$9\r\nraspberry\r\n",
                "*5\r\n$4\r\nXADD\r\n$5\r\ngrape\r\n$3\r\n0-3\r\n$10\r\nstrawberry\r\n$9\r\npineapple\r\n",
                "*4\r\n$6\r\nXRANGE\r\n$5\r\ngrape\r\n$3\r\n0-2\r\n$3\r\n0-3\r\n"
        };

        try {
            // BaseTest.forDebug(List.of(input));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}