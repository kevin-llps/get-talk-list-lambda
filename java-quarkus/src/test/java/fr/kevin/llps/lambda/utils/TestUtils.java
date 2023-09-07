package fr.kevin.llps.lambda.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TestUtils {

    public static String readFilename(String filename) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(TestUtils.class.getResourceAsStream(filename), UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
