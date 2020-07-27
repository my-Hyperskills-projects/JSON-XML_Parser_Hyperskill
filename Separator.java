package converter;

import java.util.ArrayList;

public interface Separator {
    ArrayList<String> elements = new ArrayList<>();
    int currentElementIndex = 0;

    public boolean hasNext();

    public String next();
}
