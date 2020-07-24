package com.krakenrs.spade.ir.type;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Modifier;
import java.util.Set;

public class ResolvedClassType extends ClassType {
    private final ClassType superClass;
    private final Set<ClassType> superInterfaces;
    private final int modifiers;

    public ResolvedClassType(String className, ClassType superClass, Set<ClassType> superInterfaces, int modifiers) {
        super(className);
        this.superClass = superClass;
        this.superInterfaces = requireNonNull(superInterfaces);
        this.modifiers = modifiers;
    }

    @Override
    public ClassType getSuperClassType() {
        return superClass;
    }

    @Override
    public Set<ClassType> getSuperInterfaceTypes() {
        return superInterfaces;
    }

    @Override
    public boolean isInterface() {
        return Modifier.isInterface(modifiers);
    }
}
