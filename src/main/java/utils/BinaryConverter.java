package utils;

public class BinaryConverter {
    public static String toBinary(String str) {
        byte[] bytes = str.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            var binaryString = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            binary.append(binaryString);
        }
        return binary.toString();
    }

}
