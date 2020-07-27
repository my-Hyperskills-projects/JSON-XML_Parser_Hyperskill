package converter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BraceSeparator implements Separator {

    ArrayList<String> elements;
    int currentElementIndex = 0;

    public BraceSeparator(String text) {
        if (text.matches("\\{.+}")) {
            int firstBraceIndex = text.indexOf("{");
            int lastBraceIndex = text.lastIndexOf("}");
            text = text.substring(firstBraceIndex + 1, lastBraceIndex);
        }

        elements = new ArrayList<>();
        int count = 0;
        int start = 0;
        int end;

        Pattern noBracePattern = Pattern.compile("\"[@#]?\\w+\"\\s*:\\s*(\\d+|null|true|false|\".[^,]+\"|\\[.[^\\[\\]]+\\])");
        Pattern commonBracePattern = Pattern.compile("(\"[@#]?\\w+\"\\s*:\\s*(\\d+|null|true|false|\".[^,]+\"|\\[.[^\\[\\]]+\\]|\\{))|\\}");
        Pattern closingBracePattern = Pattern.compile("}");
        Pattern openingBracePattern = Pattern.compile("\"[@#]?\\w+\"\\s*:\\s*\\{");
        Matcher commonBraceMatcher = commonBracePattern.matcher(text);

        while (commonBraceMatcher.find()) {
            String braceGroup = commonBraceMatcher.group();

            Matcher noBraceMatcher = noBracePattern.matcher(braceGroup);
            if (count == 0 && noBraceMatcher.matches()) {
                elements.add(braceGroup);
                continue;
            }

            Matcher closingBraceMatcher = closingBracePattern.matcher(braceGroup);
            Matcher openingBraceMatcher = openingBracePattern.matcher(braceGroup);
            if (closingBraceMatcher.matches()) {
                count--;
                if (count == 0) {
                    end = commonBraceMatcher.end();
                    elements.add(text.substring(start, end));
                }
            } else if (openingBraceMatcher.matches()) {
                if (count == 0) {
                    start = commonBraceMatcher.start();
                }
                count++;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return currentElementIndex < elements.size();
    }

    @Override
    public String next() {
        return elements.get(currentElementIndex++);
    }
}
