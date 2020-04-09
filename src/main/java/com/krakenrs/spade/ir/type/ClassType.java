package com.krakenrs.spade.ir.type;

import java.util.Objects;
import java.util.Set;

public abstract class ClassType implements Type {
    private final String className;
    private final ObjectType valueType;

    public ClassType(String className) {
        this.className = className.replace('.', '/');
        this.valueType = new ObjectType(this);
    }

    public String getClassName() {
        return className;
    }

    public ObjectType asValueType() {
        return valueType;
    }

    public abstract ClassType getSuperClassType();

    public abstract Set<ClassType> getSuperInterfaceTypes();

    @Override
    public String toString() {
        return className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClassType classType = (ClassType) o;
        return Objects.equals(className, classType.className) && Objects.equals(valueType, classType.valueType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, valueType);
    }
}
