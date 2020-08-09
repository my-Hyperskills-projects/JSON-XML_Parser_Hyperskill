package converter;

import java.util.ArrayList;

public interface Analyzer {

    ObjectContainer get(String text);

    void parse(String text, ObjectContainer parentContainer, ArrayList<ObjectContainer> subContainers);
}
