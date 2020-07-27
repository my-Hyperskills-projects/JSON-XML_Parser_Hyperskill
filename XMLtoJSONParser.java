package converter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLtoJSONParser implements Parser {
    int tabCount = 0;

    @Override
    public String parse(String text) {
        TagSeparator separator = new TagSeparator(text);
        StringBuilder json = new StringBuilder();
        boolean label = false;

        json.append("{\n");

        while (separator.hasNext()) {

            String tagConstruction = separator.next();

            String tagName = getTagName(tagConstruction);
            json.append(String.format("%s\"%s\" : ", getTabs(1), tagName));

            Map<String, String> tagAttributes = getTagAttributes(tagConstruction);
            if (tagAttributes != null && tagAttributes.size() > 0) {
                json.append("{\n");
                label = true;
                for (var attribute : tagAttributes.entrySet()) {
                    String key = attribute.getKey();
                    String value = attribute.getValue();
                    /**
                    if (value.matches("\"\\d+\"")) {
                        value = value.replaceAll("\"", "");
                    }
                     */
                    json.append(String.format("%s\"@%s\" : %s,\n", getTabs(2), key, value));
                }
            }

            String body = getBody(tagConstruction, tagName);

            if (body != null && haveTags(body)) {
                tabCount++;
                body = parse(body);
                tabCount--;
            } else if (body != null) {
                body = String.format("\"%s\"", body);
            }

            if (label) {
                json.append(String.format("%s\"#%s\" : %s\n", getTabs(2), tagName, body));
                json.append(getTabs(1) + "}");
            } else {
                json.append(body);
            }
            if (separator.hasNext()) {
                json.append(",\n");
            } else {
                json.append("\n");
            }
        }
        json.append(getTabs(0) + "}");

        return json.toString();
    }

    boolean haveTags(String data) {
        return data.matches("\\s*<.+>\\s*");
    }

    String getBody(String data, String tagName) {
        String regex = String.format("(?<=>).*(?=<\\/%s>)", tagName);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    String getTagName(String data) {
        Pattern pattern = Pattern.compile("(?<=<).[^<>\\s]*(?=\\s|>)");
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            String tagName = matcher.group();
            return tagName;
        }

        return null;
    }

    Map<String, String> getTagAttributes(String data) {
        Pattern pattern = Pattern.compile("(?<=\\s).[^<>]*(?=>)");
        Matcher matcher = pattern.matcher(data);
        HashMap<String, String> attributes = new HashMap<>();

        if (matcher.find()) {
            Pattern pairPattern = Pattern.compile("\\w*\\s*=\\s*\"\\w*\"");
            String attributesLine = matcher.group();
            Matcher pairMatcher = pairPattern.matcher(attributesLine);

            while (pairMatcher.find()) {
                String pair = pairMatcher.group();
                String[] attributesData = pair.split("\\s*=\\s*");
                attributes.put(attributesData[0], attributesData[1]);
            }
            return attributes;
        }

        return null;
    }

    private String getTabs(int extra) {
        StringBuilder tabs = new StringBuilder();

        for (int i = 0; i < tabCount + extra; i++) {
            tabs.append("\t");
        }

        return tabs.toString();
    }
}
