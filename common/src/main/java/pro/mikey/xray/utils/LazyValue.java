package pro.mikey.xray.utils;

import java.util.function.Supplier;

public class LazyValue<T> {
    private T value;
    private final Supplier<T> supplier;

    private LazyValue(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> LazyValue<T> of(Supplier<T> supplier) {
        return new LazyValue<>(supplier);
    }

    public T get() {
        if (value == null) {
            value = supplier.get();
        }
        return value;
    }
}
