package com.krakenrs.spade.ir.type;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleTypeManager extends TypeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTypeManager.class);

    @Override
    protected ClassType findClassType(String name) {
        try {
            Class<?> c = Class.forName(name.replace("/", "."));
            ClassType sT = c.getSuperclass() == null ? null : asClassType(c.getSuperclass());
            Set<ClassType> iTs = new HashSet<>();
            for (Class<?> i : c.getInterfaces()) {
                iTs.add(asClassType(i));
            }
            return new ResolvedClassType(name, sT, iTs, c.getModifiers());
        } catch (ClassNotFoundException e) {
            LOGGER.error("", e);
            return new UnresolvedClassType(name);
        }
    }
}
