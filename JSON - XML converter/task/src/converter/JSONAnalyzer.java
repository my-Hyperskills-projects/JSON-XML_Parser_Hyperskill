package converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONAnalyzer implements Analyzer {
    Stack<String> path;
    ObjectContainer result;
    Map<String, String> emptyMap = new LinkedHashMap<>();
    Map<String, String> attributes = new LinkedHashMap<>();
    String key = null;

    /**
     * При создании объекта класса переменной result присваивается новый ObjectContainer без параметров,
     * который выполняет роль корня(верхнего обьекта в иерархии)
     */
    public JSONAnalyzer() {
        path = new Stack<>();
        result = new ObjectContainer();
    }

    /**
     * Используется для общения с другими классами
     * @param text - json код в виде строки
     * @return - корневой объект класса ObjectContainer, которые содержит внутри все остальные
     */
    @Override
    public ObjectContainer get(String text) {
        parse(text, result, new ArrayList<>());
        return result;
    }


    /**
     * Получает дочерние элементы и присваивает их родительскому элементу
     * @param text - содержимое фигурных скобок родительского элемента
     * @param parentContainer - родительский элемент
     * @param subContainers - список с дочерними элементами
     */
    @Override
    public void parse(String text, ObjectContainer parentContainer, ArrayList<ObjectContainer> subContainers) {
        //Обьект разделяет строку на элементы, которые находятся на одной "глубине"
        JSONSeparator separator = new JSONSeparator(text);

        while (separator.hasNext()) {
            String element = separator.next();

            boolean isHaveBraces = haveBraces(element);
            boolean label = false;

            String potentialKey = getKey(element);
            if (potentialKey == null || potentialKey.matches("\\s*")) {
                continue;
            }

            //Из элемента получаются имя, атрибуты и значение, и на основе этих данных создается контейнер
            if (!potentialKey.contains("#")) {
                key = potentialKey;
                path.add(key); //Ключ заносится в стэк имитирующий путь
            }

            if (potentialKey.contains("#") && !potentialKey.matches("#" + key)) {
                key = potentialKey.replaceAll("#", "");
                path.add(key);
                label = true;
            }

            Map<String, String> potentialAttributes = getAttributes(element);

            if (potentialAttributes.size() > 0) {
                attributes = potentialAttributes;
            }

            String value = null;
            ObjectContainer container = null;
            if (isHaveBraces) {
                String content = getBracesContent(element);

                Pattern pattern = Pattern.compile("[^{}]*\"#\".*");
                Matcher matcher = pattern.matcher(content);

                if (matcher.matches()) {
                    container = new ObjectContainer((Stack<String>)path.clone(), "", attributes);
                    attributes = emptyMap; //При создании нового контейнера карта с атрибутами очищается
                } else if (potentialKey.contains("#") || !content.contains("#" + key)) {
                    container = new ObjectContainer((Stack<String>)path.clone(), attributes);
                    attributes = emptyMap;
                }

                //Если текущий элемент содержит в себе фигурные скобки, то рекурсивно вызывается эта же функция для
                //текущего элемента
                if (container != null) {
                    parse(content, container, new ArrayList<>());
                } else {
                    parse(content, parentContainer, subContainers);
                }

            } else {
                Pattern pattern = Pattern.compile("(?<=:)\\s*.+");
                Matcher matcher = pattern.matcher(element);

                if (matcher.find()) {
                    if (!matcher.group().contains("[")) {
                        value = matcher.group().replaceAll("[\"\\s]", "");

                        if (value.matches("\\s*\\{\\s*}\\s*")) {
                            value = "";
                        }
                    } else {
                        value = matcher.group();
                    }
                }

                container = new ObjectContainer((Stack<String>)path.clone(), value, attributes);
                if (value != null && value.contains("[")) {
                    container.setSpecialArray(new SpecialArray(value));
                }
                attributes = emptyMap;
            }

            //Если контейнер создан - он добавляется в список
            if (container != null) {
                subContainers.add(container);
            }

            if (!potentialKey.contains("#") || label) {
                path.pop();
            }
        }

        //Когда все дочерние элементы разобраны список с ними устанавливается в родительский контейнер
        parentContainer.setSubContainers(subContainers);
    }


    /**
     * @param data - элемент в виде строки
     * @return содержимое фигурных скобок элемента
     */
    public String getBracesContent(String data) {
        Pattern pattern = Pattern.compile("(?<=\\{).+(?=})");
        Matcher matcher = pattern.matcher(data);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    /**
     * @param data - элемент в виде строки
     * @return ключ элемета
     */
    public String getKey(String data) {
        Pattern pattern = Pattern.compile("(?<=[\"])#?\\w[^@]+(?=\")");
        Matcher matcher = pattern.matcher(data.split(":")[0]);


        if (matcher.find()) {
            return matcher.group().trim();
        }

        return null;
    }

    /**
     * @param data - элемент в виде строки
     * @return атрибуты элемента
     */
    public LinkedHashMap<String, String> getAttributes(String data) {
        Pattern pattern = Pattern.compile("\"@\\w+\"\\s*:\\s*([\\.\\d]+|null|true|false|\"[^,]*\")");

        String str = data;
        Pattern elemAtrPattern = Pattern.compile("([^{}]*\\{[^{}]*\\{|\\{[^{}]*})");
        Matcher elemAtrMatcher = elemAtrPattern.matcher(data);
        if (elemAtrMatcher.find()) {
            str = elemAtrMatcher.group();
        }

        Matcher matcher = pattern.matcher(str);
        LinkedHashMap<String, String> attributes = new LinkedHashMap<>();

        while (matcher.find()) {
            String group = matcher.group();
            String[] keyAndValue = group.split("\\s*:\\s*");
            String key = keyAndValue[0];
            String value = keyAndValue[1];

            if (key.matches("\"@\\w+\"")) {
                key = key.replaceAll("[@\"]", "");
            }

            if (value.matches("\\s*null\\s*")) {
                value = "";
            }

            if (!value.matches("\".*\"")) {
                value = "\"" + value + "\"";
            }



            attributes.put(key, value);
        }

        return attributes;
    }

    private boolean haveBraces(String data) {
        return data.matches("\"[@#]?[\\w\\.]+\"\\s*:\\s*\\{.+}");
    }
}
