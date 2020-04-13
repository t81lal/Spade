package com.krakenrs.spade.ir.value;

import java.util.Objects;

import com.krakenrs.spade.ir.value.Value.AbstractValue;

public class Local extends AbstractValue {

    private final int index;
    private final boolean isStack;

    public Local(int index, boolean isStack) {
        super(Kind.LOCAL);
        this.index = index;
        this.isStack = isStack;
    }

    public int index() {
        return index;
    }

    public boolean isStack() {
        return isStack;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, isStack);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Local other = (Local) obj;
        return other.index == index && other.isStack == isStack;
    }

    @Override
    public String toString() {
        return (isStack ? "s" : "l") + "var" + index;
    }
}
