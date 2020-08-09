package converter;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLAnalyzer implements Analyzer {
    Stack<String> path;
    ObjectContainer result;

    public XMLAnalyzer() {
        path = new Stack<>();
        result = new ObjectContainer();
    }

    /**
     * @param text - XML код в виде строки
     * @return родительский(внешний) ObjectContainer содержащий в себе все остальные
     */
    public ObjectContainer get(String text) {
        parse(text, result, new ArrayList<>()); //Передаёт родительский ObjectContainer и пустой список для наполнения его
                                                // дочерними ObjectContainer'ами
        return result;
    }

    /**
     * @param text - XMl код в виде строки
     * @param parentContainer - родительский ObjectContainer
     * @param subContainers - список для дочерних контейнеров
     */
    public void parse(String text, ObjectContainer parentContainer, ArrayList<ObjectContainer> subContainers) {
        // Делит строку на элементы с одной глубиной
        XMLSeparator separator = new XMLSeparator(text);

        // Проходится по всем элементам и получает имя, атрибуты и значение тега(ов)
        while (separator.hasNext()) {
            String tagConstruction = separator.next();
            String tagName = getTagName(tagConstruction);
            path.add(tagName);

            LinkedHashMap<String, String> tagAttributes = getTagAttributes(tagConstruction);

            String value = getBody(tagConstruction, tagName);


            //В зависимости от ситуации переменной container присваивается новый объект
            ObjectContainer container;
            if (value != null && !haveTags(value) && tagAttributes != null) {
                container = new ObjectContainer((Stack<String>)path.clone(), value, tagAttributes);
            } else if (value != null && haveTags(value) && tagAttributes != null) {
                container = new ObjectContainer((Stack<String>)path.clone(), tagAttributes);
            } else if (value != null && !haveTags(value) && tagAttributes == null) { // <---
                container = new ObjectContainer((Stack<String>)path.clone(), value);
            } else if (value == null && tagAttributes != null) {
                container = new ObjectContainer((Stack<String>)path.clone(), "null", tagAttributes);
            } else if (value == null) {
                container = new ObjectContainer((Stack<String>)path.clone(), "null");
            } else {
                container = new ObjectContainer((Stack<String>)path.clone());
            }

            // Если тек.тэги содержат в себе теги element, то контейнеру также присваивается обЪект класса представляющи этот массив
            if (value != null && haveTags(value) && value.matches("<\\s*\\/?\\s*element[^<>]*>(.*<\\s*\\/?\\s*element\\s*\\/?>)?")) {
                container.setSpecialArray(new SpecialArray(value));
            }

            // Новый контейнер добавляется в общий список дочерних контейнеров
            subContainers.add(container);

            // Если тело тегов содержит в себе ещё теги, то это тело рекурсивно парсится
            if (value != null && haveTags(value) && !value.matches("<\\s*\\/?\\s*element[^<>]*>(.*<\\s*\\/?\\s*element\\s*\\/?>)?")) {
                parse(value, container, new ArrayList<>());
            }

            path.pop();
        }

        // Список с дочерними контейнерами присваивается родительскому контейнеру
        parentContainer.setSubContainers(subContainers);
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

    LinkedHashMap<String, String> getTagAttributes(String data) {
        Pattern pattern = Pattern.compile("(?<=\\s).[^<>]*(?=>)");
        Matcher matcher = pattern.matcher(data);
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        if (matcher.find() && !data.substring(0, matcher.start()).contains(">")) {

            Pattern pairPattern = Pattern.compile("\\w*\\s*=\\s*[\"'][\\w\\.]*[\"']");
            String attributesLine = matcher.group();
            Matcher pairMatcher = pairPattern.matcher(attributesLine);

            if (pairMatcher.find()) {
                String pair = pairMatcher.group();
                String[] attributesData = pair.split("\\s*=\\s*");
                attributes.put(attributesData[0], attributesData[1]);

                while (pairMatcher.find()) {
                    pair = pairMatcher.group();
                    attributesData = pair.split("\\s*=\\s*");
                    attributes.put(attributesData[0], attributesData[1]);
                }
                return attributes;
            }
        }

        return null;
    }
}
