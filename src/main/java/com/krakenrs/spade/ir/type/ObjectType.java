package com.krakenrs.spade.ir.type;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class ObjectType implements ValueType {
    private final ClassType classType;

    public ObjectType(ClassType classType) {
        this.classType = requireNonNull(classType);
    }

    protected ObjectType() {
        this.classType = null;
    }

    public ClassType getClassType() {
        return classType;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('L');
        sb.append(classType);
        sb.append(';');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ObjectType that = (ObjectType) o;
        return Objects.equals(classType, that.classType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classType);
    }
}
