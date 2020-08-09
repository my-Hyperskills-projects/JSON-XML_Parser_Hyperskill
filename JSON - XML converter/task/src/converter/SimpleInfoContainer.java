package converter;

/**
 * Представляет простые элементы по типу null, true, false, "text" и т.д.
 */
public class SimpleInfoContainer implements InfoContainer {
    String info;

    public SimpleInfoContainer(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return info;
    }
}
