package com.krakenrs.spade.ir.type;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class ArrayType implements ValueType {
    private final ValueType elementType;

    public ArrayType(ValueType elementType) {
        this.elementType = requireNonNull(elementType);
    }

    public ValueType elementType() {
        return elementType;
    }

    public ValueType baseElementType() {
        if (elementType instanceof ArrayType) {
            return ((ArrayType) elementType).baseElementType();
        } else {
            return elementType;
        }
    }

    public int dimensions() {
        return 1 + (elementType instanceof ArrayType ? ((ArrayType) elementType).dimensions() : 0);
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public String toString() {
        return '[' + elementType.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ArrayType arrayType = (ArrayType) o;
        return Objects.equals(elementType, arrayType.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType);
    }
}
