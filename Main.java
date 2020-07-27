package converter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        String path = "test.txt";
        String line = readStringToParse(path)
                .replaceAll("(?<=[<>\"{}:/,])\\s+(?=[<>\"{}:/,])", "")
                .replaceAll("\r\n", "");
        Parser parser;

        if (line != null && line.startsWith("<")) {
            parser = new XMLtoJSONParser();
        } else {
            parser = new JSONtoXMLParser();
        }

        System.out.println(parser.parse(line));
    }

    static String readStringToParse(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            System.out.println("ERROR!!!101!");
        }
        return null;
    }
}

