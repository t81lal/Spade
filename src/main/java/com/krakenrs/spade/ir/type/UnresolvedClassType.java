package com.krakenrs.spade.ir.type;

import java.util.Set;

public class UnresolvedClassType extends ClassType {
    public UnresolvedClassType(String className) {
        super(className);
    }

    @Override public ClassType getSuperClassType() {
        throw new UnsupportedOperationException();
    }

    @Override public Set<ClassType> getSuperInterfaceTypes() {
        throw new UnsupportedOperationException();
    }
}
