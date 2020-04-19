package com.krakenrs.spade.commons.collections;

import java.util.function.Function;
import java.util.function.Supplier;

public class IdSupplier<T> implements Supplier<T> {
    private int counter;

    private final Function<Integer, T> supplier;

    public IdSupplier(Function<Integer, T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        return supplier.apply(counter++);
    }

    public void reset() {
        counter = 0;
    }
}
