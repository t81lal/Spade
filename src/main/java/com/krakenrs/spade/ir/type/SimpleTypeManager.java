package com.krakenrs.spade.ir.type;

import java.util.HashSet;
import java.util.Set;

public class SimpleTypeManager extends TypeManager {
    @Override
    protected ClassType findClassType(String name) {
        try {
            Class<?> c = Class.forName(name.replace("/", "."));
            ClassType sT = c.getSuperclass() == null ? null : asClassType(c.getSuperclass());
            Set<ClassType> iTs = new HashSet<>();
            for (Class<?> i : c.getInterfaces()) {
                iTs.add(asClassType(i));
            }
            return new ResolvedClassType(name, sT, iTs);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return new UnresolvedClassType(name);
        }
    }
}
