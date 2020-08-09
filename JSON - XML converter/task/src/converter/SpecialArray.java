package converter;

import kotlin.jvm.internal.MagicApiIntrinsics;

import java.awt.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Представляет массив элемента.
 * Может хранить в себе объекты классов ObjectContainer, SimpleInfoContainer, а также объекты собственого класса
 */
public class SpecialArray implements InfoContainer {
     ArrayList<InfoContainer> array;

    public SpecialArray(String text) {
        this.array = new ArrayList<>();
        parse(text);
    }

    private void parse(String text) {
        if (text.startsWith("<")) {
            parseXML(text);
        } else {
            parseJSON(text);
        }
    }

    /**TODO:
     * Разобрать варианты, когда элемент - сложный объект(содержит внутри тэги), протой объект или
     * массив(возможно тоже самое,что и сложный объект)
     * Проверить создаваемые обьекты
     * Для сложных обьектов:
     *    -Парсит масивы?
     */
    private void parseXML(String text) {
        ArrayList<String> elements = separateXML(text);

        for (String element : elements) {
            if (element.matches("<\\s*element[^<>]*\\s*\\/\\s*>")) {
                array.add(new SimpleInfoContainer("null"));
                continue;
            }

            Pattern pattern = Pattern.compile("(?<=>).*(?=<\\/)");
            Matcher matcher = pattern.matcher(element);

            String body = "";
            if (matcher.find()) {
                body = matcher.group();
            }

            if (body.contains("<")) {
                XMLAnalyzer analyzer = new XMLAnalyzer();
                array.add(analyzer.get(body));
            } else {
                array.add(new SimpleInfoContainer(body));
            }
        }
    }

    /**
     * <element></element>
     * <element/>
     *
     * @param text
     * @return
     */
    private ArrayList<String> separateXML(String text) {
        ArrayList<String> elements = new ArrayList<>();

        Pattern commonPattern = Pattern.compile("<\\s*\\/?\\s*element[^<>]*\\s*\\/?\\s*>");
        Matcher commonMatcher = commonPattern.matcher(text);

        int count = 0;
        int start = 0;
        int end = 0;

        while (commonMatcher.find()) {
            String group = commonMatcher.group();

            if (group.matches("<\\s*element[^<>]*\\s*\\/\\s*>")) {
                if (count == 0) {
                    elements.add(group);
                }
                continue;
            }

            if (group.matches("<\\s*[^<>/]+\\s*>")) {
                if (count == 0 && group.matches("<\\s*element[^<>/]*\\s*>")) {
                    start = commonMatcher.start();
                }
                count++;
            } else if (group.matches("<\\s*\\/\\s*[^<>/]+\\s*>")) {
                count--;
                if (count == 0 && group.matches("<\\s*\\/\\s*element\\s*>")) {
                    end = commonMatcher.end();
                    elements.add(text.substring(start, end));
                }
            }
        }

        return elements;
    }

    /**
     * Переводит элементы массива в подходящий формат и добавляет в общий список
     * [...] - SpecialArray
     * {...} - ObjectContainer
     * (текст в кавычках, числа, null, и пр.) - SimpleInfoContainer
     * @param text
     */
    private void parseJSON(String text) {
        int firstBraceIndex = text.indexOf("[");
        int lastBraceIndex = text.lastIndexOf("]");
        text = text.substring(firstBraceIndex + 1, lastBraceIndex);

        ArrayList<String> elements = separateJSON(text);

        for (String element : elements) {
            if (element.startsWith("{")) {
                Analyzer analyzer = new JSONAnalyzer();
                array.add(analyzer.get(element));
            } else if (element.startsWith("[")) {
                array.add(new SpecialArray(element));
            } else {
                array.add(new SimpleInfoContainer(element));
            }
        }
    }


    /**
     * Делит массив на элементы и возвращает их в виде списка.
     * Формат элементов: [...], {...}, (текст в кавычках, числа, null, и пр.)
     * @param text
     * @return
     */
    private ArrayList<String> separateJSON(String text) {
        ArrayList<String> elements = new ArrayList<>();

        Pattern commonPattern = Pattern.compile("\"[^\"]*\"|null|true|false|[\\d\\.]+|\\[|\\]|\\{|\\}");
        Matcher commonMatcher = commonPattern.matcher(text);

        int count = 0;
        int start = 0;
        int end;
        boolean isClosed = true;

        int countForArray = 0;
        int startForArray = 0;
        int endForArray;
        boolean isClosedArray = true;

        while (commonMatcher.find()) {
            String group = commonMatcher.group();

            if (group.matches("\\{")) {
                if (count == 0) {
                    isClosed = false;
                    start = commonMatcher.start();
                }
                count++;
                continue;
            } else if (group.matches("\\}")) {
                count--;
                if (count == 0) {
                    isClosed = true;
                    end = commonMatcher.end();
                    elements.add(text.substring(start, end));
                }
                continue;
            }

            if (!isClosed) continue;

            if (group.matches("\\[")) {
                if (countForArray == 0) {
                    isClosedArray = false;
                    startForArray = commonMatcher.start();
                }
                countForArray++;
                continue;
            } else if (group.matches("\\]")) {
                countForArray--;
                if (countForArray == 0) {
                    isClosedArray = true;
                    endForArray = commonMatcher.end();
                    elements.add(text.substring(startForArray, endForArray));
                }
                continue;
            }

            if (!isClosedArray) continue;

            elements.add(group);
        }

        return elements;
    }

    public InfoContainer get(int index) {
        return array.get(index);
    }

    public int size() {
        return array.size();
    }
}
