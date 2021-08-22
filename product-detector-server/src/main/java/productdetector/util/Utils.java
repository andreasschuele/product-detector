package productdetector.util;

import java.util.function.Consumer;

public class Utils {

    public static <T> T touch(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }
}
