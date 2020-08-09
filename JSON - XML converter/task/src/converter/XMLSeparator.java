package converter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLSeparator implements Separator {
    ArrayList<String> elements;
    int currentElementIndex = 0;

    public XMLSeparator(String text) {
        elements = new ArrayList<>();
        int count = 0;
        int start = 0;
        int end;

        Pattern commonTagPattern = Pattern.compile("<.[^<>]+>"); //<tag></tag><tag/>
        Pattern closingTagPattern = Pattern.compile("</.[^<>/]+>"); //</tag>
        Pattern noBodyTagPattern = Pattern.compile("<.[^<>/]+/>");  //<tag/>
        Matcher commonTagMatcher = commonTagPattern.matcher(text);  //</tag>

        while (commonTagMatcher.find()) {
            String tag = commonTagMatcher.group();
            Matcher noBodyTagMatcher = noBodyTagPattern.matcher(tag);
            if (noBodyTagMatcher.matches()) {
                if (count == 0) {
                    elements.add(tag);
                }
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
