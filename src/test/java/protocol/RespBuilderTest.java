package protocol;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RespBuilderTest {

    @Test
    void simpleString() {
        assertEquals("+OK\r\n", RespBuilder.simpleString("OK"));
    }

    @Test
    void error() {
        assertEquals("-ERR unknown command\r\n", RespBuilder.error("unknown command"));
    }

    @Test
    void integer() {
        assertEquals(":10\r\n", RespBuilder.integer(10));
    }

    @Test
    void bulkString() {
        assertEquals("$5\r\nhello\r\n", RespBuilder.bulkString("hello"));
    }

    @Test
    void nullBulkString() {
        assertEquals("$-1\r\n", RespBuilder.bulkString(null));
    }

    @Test
    void arrayMixedTypes() {
        String actual = RespBuilder.array(
                List.of(
                        RespBuilder.simpleString("OK"),
                        RespBuilder.integer(1),
                        RespBuilder.bulkString("bar")));

        assertEquals("*3\r\n+OK\r\n:1\r\n$3\r\nbar\r\n", actual);
    }
}