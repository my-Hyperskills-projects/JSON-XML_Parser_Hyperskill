package converter;

import java.util.ArrayList;
import java.util.Map;

public class Converter {

    static int tabCount = 0;

    public static String getJSON(ObjectContainer container) {
        StringBuilder json = new StringBuilder();
        convertToJSON(json, container, true);
        return json.toString();
    }

    private static void convertToJSON(StringBuilder json, ObjectContainer container, boolean isLast) {
        if (container.isRoot) {
            String tabs = getTabs();
            json.append(tabs + "{\n");
            tabCount++;
            addSubContainersToJSON(json, container);
            json.append(tabs + "}");

            if (!isLast) {
                json.append(",\n");
            } else {
                json.append("\n");
            }
            tabCount--;
        } else {
            json.append(getTabs() + "\"" + container.getName() + "\" : ");

            if (container.haveAttributes()) {
                addAttributesToJSON(json, container, isLast);
            } else {
                addValueToJSON(json, container, isLast);
            }
        }
    }

    private static void addAttributesToJSON(StringBuilder json, ObjectContainer container, boolean isLast) {
        Map<String, String> attributes = container.getAttributes();
        String tags1 = getTabs();

        json.append("{\n");
        tabCount++;
        String tags2 = getTabs();

        for (var attribute : attributes.entrySet()) {
            String key = tags2 + "\"@" + attribute.getKey() + "\"";
            String value;
            if (attribute.getValue().matches("null|true|false|\"\\s*\"") || attribute.getValue().contains("\"")) {
                value = attribute.getValue();
            } else if (attribute.getValue().contains("'")) {
                value = attribute.getValue().replaceAll("'", "\"");
            } else {
                value = "\"" + attribute.getValue() + "\"";
            }
            json.append(key + " : " + value + ",\n");
        }

        json.append(tags2 + "\"#" + container.getName() + "\" : ");
        addValueToJSON(json, container, true);

        json.append(tags1 + "}");

        if (container.haveSubContainers() && container.getValue() != null) {
            json.append(",\n");
            addSubContainersToJSON(json, container);
        } else if (!isLast) {
            json.append(",\n");
        } else {
            json.append("\n");
        }

        tabCount--;
    }

    private static void addValueToJSON(StringBuilder json, ObjectContainer container, boolean isLast) {
        String value = container.getValue();

        if (container.haveArray()) {
            SpecialArray array = container.getSpecialArray();
            convertArrayToJSON(json, array);
        } else if (value != null) {
            if (value.equals("null")) {
                json.append(value);
            } else {
                json.append("\"" + value + "\"");
            }
        } else {
            if (container.haveSubContainers()) {
                String tags = getTabs();
                json.append("{\n");
                tabCount++;
                addSubContainersToJSON(json, container);
                json.append(tags + "}");
                tabCount--;
            } else {
                json.append("null");
            }
        }

        if (!isLast) {
            json.append(",\n");
        } else {
            json.append("\n");
        }
    }

    private static void convertArrayToJSON(StringBuilder json, SpecialArray array) {
        json.append("[\n");
        tabCount++;
        for (int i = 0; i < array.size(); i++) {
            InfoContainer infoContainer = array.get(i);
            if (infoContainer instanceof SimpleInfoContainer) {
                if (infoContainer.toString().equals("null")) {
                    json.append(getTabs() + infoContainer.toString());
                } else {
                    json.append(getTabs() + "\"" + infoContainer.toString() + "\"");
                }

                if (i < array.size() - 1) {
                    json.append(",\n");
                } else {
                    json.append("\n");
                }
            } else if (infoContainer instanceof ObjectContainer) {
                convertToJSON(json, (ObjectContainer) infoContainer, i == array.size() - 1);
            } else if (infoContainer instanceof SpecialArray) {
                convertArrayToJSON(json, (SpecialArray) infoContainer);
                if (i < array.size() - 1) {
                    json.append(",\n");
                } else {
                    json.append("\n");
                }
            }
        }
        tabCount--;
        json.append(getTabs() + "]");
    }

    private static void print(ObjectContainer container) {
        System.out.println(container);

        if (container.haveSubContainers()) {
            ArrayList<ObjectContainer> subs = container.getSubContainers();
            for (ObjectContainer sub : subs) {
                print(sub);
            }
        }
    }

    private static void addSubContainersToJSON(StringBuilder json, ObjectContainer container) {
        ArrayList<ObjectContainer> subContainers = container.getSubContainers();

        for (ObjectContainer subContainer : subContainers) {
            if (subContainers.indexOf(subContainer) == subContainers.size() - 1) {
                convertToJSON(json, subContainer, true);
            } else {
                convertToJSON(json, subContainer, false);
            }
        }
    }


    public static String getXML(ObjectContainer container) {
        StringBuilder xml = new StringBuilder();
        convertToXML(xml, container);
        return xml.toString();
    }

    private static void convertToXML(StringBuilder xml, ObjectContainer container) {
        if (container.isRoot) {
            if (container.getSubContainersCount() > 1) {
                xml.append("<root>\n");
                tabCount++;
                addSubContainersToXML(xml, container);
                xml.append("</root>");
                tabCount--;
            } else {
                addSubContainersToXML(xml, container);
            }
        } else {
            convertObjectToXML(xml, container);
        }
    }

    private static void convertObjectToXML(StringBuilder xml, ObjectContainer container) {
        String tagContent = getTagContentForXML(container);
        String tabs = getTabs();
        String value = container.getValue();
        String name;

        if (container.isRoot) {
            name = "element";
        } else {
            name = container.getName();
        }

        if (container.haveArray()) {
            SpecialArray array = container.getSpecialArray();
            if (array.size() > 0) {
                xml.append(tabs + String.format("<%s>\n", tagContent));
                tabCount++;
                convertArrayToXML(xml, array);
                tabCount--;
                xml.append(tabs + String.format("</%s>\n", name));
            } else {
                xml.append(tabs + String.format("<%s></%s>\n", tagContent, name));
            }
        } else if (container.haveSubContainers() && container.getSubContainersCount() > 0) {
            xml.append(tabs + String.format("<%s>\n", tagContent));
            tabCount++;
            addSubContainersToXML(xml, container);
            xml.append(tabs + String.format("</%s>\n", name));
            tabCount--;
        } else if (value != null && !value.equals("null")) {
            xml.append(tabs + String.format("<%s>%s</%s>\n", tagContent, value, container.getName()));
        } else if (value == null || value.equals("null")) {
            xml.append(tabs + String.format("<%s/>\n", tagContent));
        }
    }

    private static void convertArrayToXML(StringBuilder xml, SpecialArray array) {
        String tabs = getTabs();

        for (int i = 0; i < array.size(); i++) {
            InfoContainer infoContainer = array.get(i);

            if (infoContainer instanceof SimpleInfoContainer) {
                String content = infoContainer.toString();
                if (content.contains("null")) {
                    xml.append(tabs + "<element/>\n");
                } else {
                    if (content.matches("\"\\s*\"")) {
                        content = content.replaceAll("\"", "");
                    }
                    xml.append(tabs + String.format("<element>%s</element>\n", content));
                }
            } else if (infoContainer instanceof ObjectContainer) {
                convertObjectToXML(xml, (ObjectContainer) infoContainer);
            } else if (infoContainer instanceof SpecialArray) {
                convertArrayToXML(xml, (SpecialArray) infoContainer);
            }
        }
    }

    private static String getTagContentForXML(ObjectContainer container) {
        String name;
        if (container.isRoot) {
            name = "element";
        } else {
            name = container.getName();
        }

        StringBuilder tagContent = new StringBuilder(name);

        if (container.haveAttributes()) {
            Map<String, String> attributes = container.getAttributes();
            for (var attribute : attributes.entrySet()) {
                String key = attribute.getKey();
                String value = attribute.getValue();
                if (!value.contains("\"")) {
                    value = String.format("\"%s\"", value);
                }

                tagContent.append(" " + key + "=" + value);
            }
        }

        return tagContent.toString();
    }

    private static void addSubContainersToXML(StringBuilder xml, ObjectContainer container) {
        ArrayList<ObjectContainer> subContainers = container.getSubContainers();

        for (ObjectContainer subContainer : subContainers) {
            convertToXML(xml, subContainer);
        }
    }

    private static String getTabs() {
        StringBuilder tabs = new StringBuilder();
        for (int i = 0; i < tabCount; i++) {
            tabs.append("\t");
        }
        return tabs.toString();
    }
}
