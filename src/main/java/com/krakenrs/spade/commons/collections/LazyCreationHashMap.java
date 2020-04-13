package com.krakenrs.spade.commons.collections;

import java.util.HashMap;
import java.util.function.Supplier;

public class LazyCreationHashMap<K, V> extends HashMap<K, V> {
    private final Supplier<V> valueCreator;

    public LazyCreationHashMap(Supplier<V> valueCreator) {
        this.valueCreator = valueCreator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object k) {
        V v = super.get(k);

        if (v != null) {
            return v;
        }

        v = valueCreator.get();
        put((K) k, v);
        return v;
    }
}
