package converter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagSeparator implements Separator {
    ArrayList<String> elements;
    int currentElementIndex = 0;

    public TagSeparator(String text) {
        elements = new ArrayList<>();
        int count = 0;
        int start = 0;
        int end;

        Pattern commonTagPattern = Pattern.compile("<.[^<>]+>");
        Pattern closingTagPattern = Pattern.compile("</.[^<>/]+>");
        Pattern noBodyTagPattern = Pattern.compile("<.[^<>/]+/>");
        Matcher commonTagMatcher = commonTagPattern.matcher(text);

        while (commonTagMatcher.find()) {
            String tag = commonTagMatcher.group();

            Matcher noBodyTagMatcher = noBodyTagPattern.matcher(tag);
            if (count == 0 && noBodyTagMatcher.matches()) {
                elements.add(tag);
                continue;
            }

            Matcher closingTagMatcher = closingTagPattern.matcher(tag);
            if (!closingTagMatcher.matches()) {
                if (count == 0) {

                    start = commonTagMatcher.start();
                }
                count++;
            } else {
                count--;
                if (count == 0) {
                    end = commonTagMatcher.end();

                    elements.add(text.substring(start, end));
                }
            }
        }
    }

    public boolean hasNext() {
        return currentElementIndex < elements.size();
    }

    public String next() {
        return elements.get(currentElementIndex++);
    }
}
