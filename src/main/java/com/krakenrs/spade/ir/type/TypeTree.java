package com.krakenrs.spade.ir.type;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.krakenrs.spade.commons.collections.LazyCreationHashMap;

import lombok.NonNull;

public class TypeTree {
    private final Map<ClassType, Set<ClassType>> subclasses;
    private final Map<ClassType, Set<ClassType>> subInterfaces;
    private final Map<ClassType, Set<ClassType>> implementors;

    public TypeTree() {
        subclasses = new LazyCreationHashMap<>(HashSet::new);
        subInterfaces = new LazyCreationHashMap<>(HashSet::new);
        implementors = new LazyCreationHashMap<>(HashSet::new);
    }

    public void addClass(@NonNull ClassType clazz) {
        if (!clazz.isInterface()) {
            ClassType superClazz = clazz.getSuperClassType();
            if (superClazz != null) {
                subclasses.get(superClazz).add(clazz);
            }
        }

        for (ClassType superInterface : clazz.getSuperInterfaceTypes()) {
            if (superInterface.isInterface()) {
                subInterfaces.get(superInterface).add(clazz);
            } else {
                implementors.get(superInterface).add(clazz);
            }
        }
    }
}
