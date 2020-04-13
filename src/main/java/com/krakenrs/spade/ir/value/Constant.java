package com.krakenrs.spade.ir.value;

import java.util.Objects;

import com.krakenrs.spade.ir.type.ValueType;
import com.krakenrs.spade.ir.value.Value.AbstractValue;

public class Constant<T> extends AbstractValue {

    private final T value;
    private final ValueType type;

    public Constant(T value, ValueType type) {
        super(Kind.CONST);
        this.value = value;
        this.type = type;
    }

    public T value() {
        return value;
    }

    public ValueType type() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Constant<?> other = (Constant<?>) obj;
        return Objects.equals(value, other.value) && Objects.equals(type, other.type);
    }

    @Override
    public String toString() {
        if (value == null) {
            return "nullconst";
        } else if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof Character) {
            return "'" + value + "'";
        } else {
            return value.toString();
        }
    }
}
