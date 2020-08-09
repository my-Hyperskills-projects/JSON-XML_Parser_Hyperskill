package converter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONSeparator implements Separator {

    ArrayList<String> elements;
    int currentElementIndex = 0;

    public JSONSeparator(String text) {

        //Если элемент корневой, удаляет внешние скобки
        if (text.matches("\\{.+}")) {
            int firstBraceIndex = text.indexOf("{");
            int lastBraceIndex = text.lastIndexOf("}");
            text = text.substring(firstBraceIndex + 1, lastBraceIndex);
        }

        elements = new ArrayList<>();
        boolean label = false;
        int arrayEndingIndex = -1;
        int count = 0;
        int start = 0;
        int end;

        Pattern noBracePattern = Pattern.compile("\"[@#]?[\\w\\.]+\"\\s*:\\s*([\\d\\.]+|null|true|false|\"[^,]*\")");
        Pattern commonBracePattern = Pattern.compile("(\"[@#]?[\\w\\.]*\"\\s*:\\s*([\\d\\.]+|null|true|false|\"[^,]*\"|\\[|\\{))|\\}");
        Pattern closingBracePattern = Pattern.compile("}");
        Pattern openingBracePattern = Pattern.compile("\"[@#]?[\\w\\.]+\"\\s*:\\s*\\{");
        Pattern arrayPattern = Pattern.compile("\"[@#]?[\\w\\.]*\"\\s*:\\s*\\[");
        Matcher commonBraceMatcher = commonBracePattern.matcher(text);

        /**
         * Находим все вхождения искомых паттернов и, в зависимости от конкретного паттерна, действуем
         */
        while (commonBraceMatcher.find()) {
            if (commonBraceMatcher.start() < arrayEndingIndex) {
                continue;
            }

            String braceGroup = commonBraceMatcher.group();

            if (braceGroup.matches("\"[@#]?\"\\s*:.*")) {
                if (braceGroup.contains("{") && !braceGroup.contains("[")) {
                    if (count == 0) {
                        label = true;
                    }
                    count++;
                }
                continue;
            }

            //Если элемент простой(не содержит скобок) и глубина нулевая - он добавляется в конечный список
            Matcher noBraceMatcher = noBracePattern.matcher(braceGroup);
            if (count == 0 && noBraceMatcher.matches()) {
                elements.add(braceGroup);
                continue;
            }

            //Если элемент содержит массив, то находится индекс закрывающей квадратной скобки и отрезок переданого текста
            // с массивом добавляется в список элементов
            Matcher arrayMatcher = arrayPattern.matcher(braceGroup);
            if (arrayMatcher.matches()) {
                int startingIndex = commonBraceMatcher.start();
                arrayEndingIndex = findArrayEndingIndex(text, startingIndex);

                if (count == 0) {
                    elements.add(text.substring(startingIndex, arrayEndingIndex));
                }
                continue;
            }


            /**
             * При нахождении элемента с отркрывающей фигурной скобкой устанавливается индекс начала элемента и
             * с каждой новой открывающей фигурной скобкой глубина увеличивается.
             * А при нахождении закрывающей фигурной скобки, соответствено уменьшается и если достигается нулевая глубина,
             * то устанавливается конечное значение и в результирующий список добавляется отрезок переданого текста
             * от установленого начала до конца
             */
            Matcher closingBraceMatcher = closingBracePattern.matcher(braceGroup);
            Matcher openingBraceMatcher = openingBracePattern.matcher(braceGroup);
            if (closingBraceMatcher.matches()) {
                count--;
                if (count == 0) {
                    if (label) {
                        label = false;
                    } else {
                        end = commonBraceMatcher.end();
                        elements.add(text.substring(start, end));
                    }
                }
            } else if (openingBraceMatcher.matches()) {
                if (count == 0) {
                    start = commonBraceMatcher.start();
                }
                count++;
            }
        }
    }

    //Имеются ли ещё элементы?
    @Override
    public boolean hasNext() {
        return currentElementIndex < elements.size();
    }

    //Следующий элемент
    @Override
    public String next() {
        return elements.get(currentElementIndex++);
    }

    /**
     * @param text - переданный текст
     * @param startingIndex - начальный индекс
     * @return индекс закрывающей квадратной скобки
     */
    private int findArrayEndingIndex(String text, int startingIndex) {
        ArrayList<Integer> openingBraceIndexes = new ArrayList<>();
        ArrayList<Integer> closingBraceIndexes = new ArrayList<>();

        //Заполняются списки с открывающими и закрывающими квадратными скобками
        int workIndex = startingIndex;
        while (true) {
            workIndex = text.indexOf("[", workIndex + 1);
            if (workIndex != -1) {
                openingBraceIndexes.add(workIndex);
            } else {
                break;
            }
        }

        workIndex = startingIndex;
        while (true) {
            workIndex = text.indexOf("]", workIndex + 1);
            if (workIndex != -1) {
                closingBraceIndexes.add(workIndex + 1);
            } else {
                break;
            }
        }

        //Пока закрывающая скобка с тек. индексом имеет перед собой большее кол-во открывающих скобок, чем тек.индекс + 1
        //переменной тек.индекса присваивается значение кол-во открывающих скобок -1
        int currentClosingBraceIndex = 0;
        while (true) {

            int count = 0;
            while (count < openingBraceIndexes.size() && currentClosingBraceIndex < closingBraceIndexes.size() &&
                    closingBraceIndexes.get(currentClosingBraceIndex) > openingBraceIndexes.get(count)) {
                count++;
            }

            if (currentClosingBraceIndex + 1 == count) {
                break;
            } else {
                currentClosingBraceIndex = count - 1;
            }
        }

        return closingBraceIndexes.get(currentClosingBraceIndex);
    }
}
