package com.krakenrs.spade.ir.type;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

import java.util.List;

public final class PrimitiveType implements ValueType {
    public static final PrimitiveType LONG = new PrimitiveType("J", 2);
    public static final PrimitiveType DOUBLE = new PrimitiveType("D", 2);
    public static final PrimitiveType FLOAT = new PrimitiveType("F", 1);
    public static final PrimitiveType INT = new PrimitiveType("I", 1, true);
    public static final PrimitiveType SHORT = new PrimitiveType("S", 1, true);
    public static final PrimitiveType BYTE = new PrimitiveType("B", 1, true);
    public static final PrimitiveType CHAR = new PrimitiveType("C", 1, true);
    public static final PrimitiveType BOOLEAN = new PrimitiveType("Z", 1);
    public static final PrimitiveType VOID = new PrimitiveType("V", 0);
    public static final PrimitiveType NULL = new PrimitiveType("null", 1);

    public static final Map<String, PrimitiveType> DESCRIPTORS = List
            .of(LONG, DOUBLE, FLOAT, INT, SHORT, BYTE, CHAR, BOOLEAN, VOID, NULL).stream()
            .collect(Collectors.toUnmodifiableMap(PrimitiveType::getDescriptor, Function.identity()));

    private final String descriptor;
    private final int size;
    private final boolean isIntLike;

    private PrimitiveType(String descriptor, int size) {
        this(descriptor, size, false);
    }

    private PrimitiveType(String descriptor, int size, boolean isIntLike) {
        this.descriptor = requireNonNull(descriptor);
        this.size = size;
        this.isIntLike = isIntLike;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public boolean isIntLike() {
        return isIntLike;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return descriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PrimitiveType that = (PrimitiveType) o;
        return size == that.size && Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descriptor, size);
    }
}
