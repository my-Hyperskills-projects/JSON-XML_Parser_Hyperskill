package converter;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

/**
 * Представляет собой элементы с ключем и значением (объекты).
 * Если не является корнем хранит в себе путь и значение(возможно null)
 * Также может хранить в себе такие же ObjectContainer'ы (дочерние), атрибуты и массив
 * Корневой элемент может содержать только дочернии контейнеры
 */
public class ObjectContainer implements InfoContainer {
    ArrayList<ObjectContainer> subContainers = null;
    SpecialArray specialArray = null;
    boolean isRoot;

    Stack<String> path;
    String value = null;
    Map<String, String> attributes = null;

    public ObjectContainer(Stack<String> path, String value, Map<String, String> attributes) {
        this.path = path;
        this.value = value;
        this.attributes = attributes;
        isRoot = false;
    }

    public ObjectContainer(Stack<String> path, Map<String, String> attributes) {
        this.path = path;
        this.attributes = attributes;
        isRoot = false;
    }

    public ObjectContainer(Stack<String> path, String value) {
        this.path = path;
        this.value = value;
        isRoot = false;
    }

    public ObjectContainer(Stack<String> path) {
        this.path = path;
    }

    public ObjectContainer() {
        this.isRoot = true;
    }

    public void setSpecialArray(SpecialArray specialArray) {
        this.specialArray = specialArray;
    }

    public void setSubContainers(ArrayList<ObjectContainer> subContainers) {
        this.subContainers = subContainers;
    }

    @Override
    public String toString() {
        if (!isRoot) {
            StringBuilder result = new StringBuilder("\nElement:\npath = " + path.toString().replaceAll("\\[|]", ""));

            if (haveArray()) {
                result.append("Array:\n");

                for (int i = 0; i < specialArray.size(); i++) {
                    result.append(specialArray.get(i) + " ");
                }

                result.append("\n");
            } else if (value != null) {
                if (value.equals("null")) {
                    result.append(String.format("\nvalue = %s", value));
                } else {
                    result.append(String.format("\nvalue = \"%s\"", value));
                }
            }

            if (attributes != null && attributes.size() > 0) {
                StringBuilder attributesStr = new StringBuilder("\nattributes:");
                for (var attribute : attributes.entrySet()) {
                    attributesStr.append("\n" + attribute.getKey() + " = " + attribute.getValue());
                }
                result.append(attributesStr.toString());
            }

            return result.toString();
        }
        return "root";
    }

    public String getName() {
        return path.peek();
    }

    public String getValue() {
        return value;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public ArrayList<ObjectContainer> getSubContainers() {
        return subContainers;
    }

    public boolean haveSubContainers() {
        return subContainers != null;
    }

    public boolean haveArray() {
        return specialArray != null;
    }

    public boolean haveAttributes() {
        return attributes != null;
    }

    public int getSubContainersCount() {
        return subContainers.size();
    }

    public SpecialArray getSpecialArray() {
        return specialArray;
    }
}