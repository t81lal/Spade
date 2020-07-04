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
        if(version < 0) {
        	throw new IllegalArgumentException("version must be positive");
        }
        this.version = version;
    }

    public Local(int index, boolean isStack) {
        super(Kind.LOCAL);
        this.index = index;
        this.isStack = isStack;
        this.version = -1;
    }

    public int index() {
        return index;
    }

    public boolean isStack() {
        return isStack;
    }
    
    public boolean isVersioned() {
    	return version >= 0;
    }

    public int version() {
    	if(version < 0) {
    		throw new UnsupportedOperationException("Unversioned local");
    	}
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
    	if(isVersioned()) {
            return String.format("%cvar%d_%d", isStack ? 's' : 'l', index, version);
    	} else {
            return String.format("%cvar%d", isStack ? 's' : 'l', index);
    	}
    }
}
