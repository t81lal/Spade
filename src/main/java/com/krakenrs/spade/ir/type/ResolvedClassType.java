package com.krakenrs.spade.ir.type;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ResolvedClassType extends ClassType {
    private final ClassType superClass;
    private final Set<ClassType> superInterfaces;

    public ResolvedClassType(String className, ClassType superClass, Set<ClassType> superInterfaces) {
        super(className);
        this.superClass = superClass;
        this.superInterfaces = requireNonNull(superInterfaces);
    }

    @Override
    public ClassType getSuperClassType() {
        return superClass;
    }

    @Override
    public Set<ClassType> getSuperInterfaceTypes() {
        return superInterfaces;
    }
}
