package converter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONtoXMLParser implements Parser {
    int tabCount = 0;

    @Override
    public String parse(String text) {
        BraceSeparator separator = new BraceSeparator(text);
        StringBuilder xml = new StringBuilder();

        while (separator.hasNext()) {
            String element = separator.next();
            boolean isHaveBraces = haveBraces(element);

            String key = getKey(element);
            if (key == null) {                                              //Может вернуть null, если тек.элемент является атрибутом(начинается с @)
                continue;
            }

            Map<String, String> attributes = getAttributes(element);
            String value = null;
            if (isHaveBraces) {
                String content = getBracesContent(element);
                isHaveBraces = haveBraces(content);
                tabCount++;
                value = parse(content);
                tabCount--;
            } else {
                Pattern pattern = Pattern.compile("(?<=:)\\s*.+");
                Matcher matcher = pattern.matcher(element);
                if (matcher.find()) {
                    value = matcher.group();
                }
            }

            if (key.contains("#")) {
                tabCount--;
                return value;
            }

            String tag = constructTag(key, value, attributes, isHaveBraces);
            xml.append(tag);
        }

        return xml.toString();
    }

    public String getBracesContent(String data) {
        Pattern pattern = Pattern.compile("(?<=\\{).+(?=})");
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    public String getKey(String data) {
        Pattern pattern = Pattern.compile("(?<=[\"])#?\\w[^@]+(?=\")");
        Matcher matcher = pattern.matcher(data.split(":")[0]);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    public Map<String, String> getAttributes(String data) {
        Pattern pattern = Pattern.compile("\"@\\w+\"\\s*:\\s*(\\d+|null|true|false|\".[^,]+\")");

        String str = data;
        Pattern elemAtrPattern = Pattern.compile(".*\\{.*\\{");
        Matcher elemAtrMatcher = elemAtrPattern.matcher(data);
        if (elemAtrMatcher.find()) {
            str = elemAtrMatcher.group();
        }

        Matcher matcher = pattern.matcher(str);
        HashMap<String, String> attributes = new HashMap<>();

        while (matcher.find()) {
            String group = matcher.group();
            String[] keyAndValue = group.split("\\s*:\\s*");
            String key = keyAndValue[0];
            String value = keyAndValue[1];

            if (key.matches("\"@\\w+\"")) {
                key = key.substring(1, key.length() - 1);
            }

            if (!value.matches("\".*\"")) {
                value = "\"" + value + "\"";
            }

            attributes.put(key, value);
        }

        return attributes;
    }

    private boolean haveBraces(String data) {
        return data.matches("\"[@#]?\\w+\"\\s*:\\s*\\{.+}");
    }

    private String constructTag(String key, String value, Map<String, String> attributes, boolean isHaveTags) {
        StringBuilder attributesStr = new StringBuilder();
        for (var attribute : attributes.entrySet()) {
            attributesStr.append(attribute.getKey() + " = " + attribute.getValue() + " ");
        }

        String tagContent = key + " " + attributesStr.toString().replaceAll("@", "");
        tagContent = tagContent.trim();
        value = value.replaceAll("\"", "").trim();

        String tabs = getTabs(0);

        if (value == null || value.contains("null")) {
            return String.format("%s<%s/>\n", tabs, tagContent);
        } else if (isHaveTags) {
            return String.format("%s<%s>\n%s%s%s</%s>\n", tabs, tagContent, tabs, value, tabs, key);
        } else {
            return String.format("%s<%s>%s</%s>\n", tabs, tagContent, value, key);
        }
    }

    private String getTabs(int extra) {
        StringBuilder tabs = new StringBuilder();

        for (int i = 0; i < tabCount + extra; i++) {
            tabs.append("\t");
        }

        return tabs.toString();
    }
}
