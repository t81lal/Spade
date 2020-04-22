package com.krakenrs.spade.ir.value;

import java.util.Objects;

import com.krakenrs.spade.ir.value.Value.AbstractValue;

public class Local extends AbstractValue {

    private final int index;
    private final boolean isStack;
    private final int version;

    public Local(int index, boolean isStack, int version) {
        super(Kind.LOCAL);
        this.index = index;
        this.isStack = isStack;
        this.version = version;
    }

    public Local(int index, boolean isStack) {
        this(index, isStack, 0);
    }

    public int index() {
        return index;
    }

    public boolean isStack() {
        return isStack;
    }

    public int version() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, isStack, version);
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
        return other.index == index && other.isStack == isStack && other.version == version;
    }

    @Override
    public String toString() {
        return String.format("%cvar%d_%d", isStack ? 's' : 'l', index, version);
    }
}
