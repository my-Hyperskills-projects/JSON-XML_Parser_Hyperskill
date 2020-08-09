package converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        boolean isJSON = false;
        String path = "test.txt";
        String line = readStringToParse(path)
                .replaceAll("(?<=[<>\"{}:/,])\\s+(?=[<>\"{}:/,])", "")
                .replaceAll("\n", "");


        Analyzer analyzer;
        if (line.startsWith("<")) {
            analyzer = new XMLAnalyzer();
        } else {
            isJSON = true;
            analyzer = new JSONAnalyzer();
        }

        ObjectContainer mainContainer = analyzer.get(line);

        if (isJSON) {
            System.out.println(Converter.getXML(mainContainer));
        } else {
            System.out.println(Converter.getJSON(mainContainer));
        }
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

