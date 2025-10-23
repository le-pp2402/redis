package test.stream;

import java.util.List;

import org.junit.jupiter.api.Test;

import test.BaseTest;

public class ExecCommand {
    @Test
    public void ExecWithoutMulti() {
        String input = "*1\r\n$4\r\nEXEC\r\n";
        try {
            List<String> inputList = List.of(input);
            BaseTest.forDebug(inputList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
