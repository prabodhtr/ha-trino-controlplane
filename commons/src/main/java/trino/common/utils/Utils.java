package trino.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class Utils {
    public static String readFileToString(String fileName) {
        try (InputStream resourceAsStream = Utils.class.getClassLoader().getResourceAsStream(fileName)) {
            if(resourceAsStream == null){
                throw new IOException("Resource not found!");
            }
            return new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        }

}
