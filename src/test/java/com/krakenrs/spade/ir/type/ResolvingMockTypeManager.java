package com.krakenrs.spade.ir.type;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ResolvingMockTypeManager extends MockTypeManager {

    @Override
    protected ClassType findClassType0(String name) {
        Objects.requireNonNull(name);

        String klassName = name.replace(".", "/");
        String clazzName = name.replace("/", ".");
        try {
            Class<?> clazz = Class.forName(clazzName);
            Class<?> superClazz = clazz.getSuperclass();
            ClassType superClassType = null;
            if (superClazz != null) {
                // Object and interfaces
                superClassType = asClassType(superClazz);
            }
            Set<ClassType> superInterfacesTypes = Arrays.asList(clazz.getInterfaces()).stream().map(this::asClassType)
                    .collect(Collectors.toSet());
            return new ResolvedClassType(klassName, superClassType, superInterfacesTypes, clazz.getModifiers());
        } catch (ClassNotFoundException e) {
            return new UnresolvedClassType(klassName);
        }
    }
}
