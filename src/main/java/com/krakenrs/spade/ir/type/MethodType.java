package com.krakenrs.spade.ir.type;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class MethodType implements Type {
    private final List<ValueType> paramTypes;
    private final ValueType returnType;

    public MethodType(List<ValueType> paramTypes, ValueType returnType) {
        this.paramTypes = requireNonNull(paramTypes);
        this.returnType = requireNonNull(returnType);
    }

    public List<ValueType> getParamTypes() {
        return paramTypes;
    }

    public ValueType getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        paramTypes.forEach(sb::append);
        sb.append(')');
        sb.append(returnType);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MethodType that = (MethodType) o;
        return Objects.equals(paramTypes, that.paramTypes) && Objects.equals(returnType, that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paramTypes, returnType);
    }
}
