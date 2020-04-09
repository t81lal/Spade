package com.krakenrs.spade.ir.type;

import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class PrimitiveType implements ValueType {
    public static final PrimitiveType LONG = new PrimitiveType("J", 2);
    public static final PrimitiveType DOUBLE = new PrimitiveType("D", 2);
    public static final PrimitiveType FLOAT = new PrimitiveType("F", 1);
    public static final PrimitiveType INT = new PrimitiveType("I", 1);
    public static final PrimitiveType SHORT = new PrimitiveType("S", 1);
    public static final PrimitiveType BYTE = new PrimitiveType("B", 1);
    public static final PrimitiveType CHAR = new PrimitiveType("C", 1);
    public static final PrimitiveType BOOLEAN = new PrimitiveType("Z", 1);
    public static final PrimitiveType VOID = new PrimitiveType("V", 0);

    public static final Map<String, PrimitiveType> DESCRIPTORS = Map
            .of(LONG.descriptor, LONG, DOUBLE.descriptor, DOUBLE, FLOAT.descriptor, FLOAT, INT.descriptor, INT,
                    SHORT.descriptor, SHORT, BYTE.descriptor, BYTE, CHAR.descriptor, CHAR, BOOLEAN.descriptor, BOOLEAN,
                    VOID.descriptor, VOID);

    private final String descriptor;
    private final int size;

    private PrimitiveType(String descriptor, int size) {
        this.descriptor = requireNonNull(descriptor);
        this.size = size;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @Override public int getSize() {
        return size;
    }

    @Override public String toString() {
        return descriptor;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PrimitiveType that = (PrimitiveType) o;
        return size == that.size && Objects.equals(descriptor, that.descriptor);
    }

    @Override public int hashCode() {
        return Objects.hash(descriptor, size);
    }
}
