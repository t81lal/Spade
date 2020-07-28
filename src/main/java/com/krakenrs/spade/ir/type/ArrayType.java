package com.krakenrs.spade.ir.type;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class ArrayType implements ValueType {
    private final ValueType componentType;

    public ArrayType(ValueType componentType) {
        this.componentType = requireNonNull(componentType);
    }

    public ValueType elementType() {
        return componentType;
    }

    public ValueType componentType() {
        if (componentType instanceof ArrayType) {
            return ((ArrayType) componentType).componentType();
        } else {
            return componentType;
        }
    }

    public int dimensions() {
        return 1 + (componentType instanceof ArrayType ? ((ArrayType) componentType).dimensions() : 0);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public String toString() {
        return '[' + componentType.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ArrayType arrayType = (ArrayType) o;
        return Objects.equals(componentType, arrayType.componentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(componentType);
    }
}
