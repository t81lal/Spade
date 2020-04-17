package com.krakenrs.spade.ir.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MockTypeManager extends TypeManager {
    private final Map<String, ClassType> classes = new HashMap<>();

    public MockTypeManager() {
    }

    public void addClass(String className, ClassType classType) {
        classes.put(className, classType);
    }

    protected ClassType findClassType0(String name) {
        return new UnresolvedClassType(name);
    }

    @Override
    public ClassType findClassType(String name) {
        // Can't use computeIfAbsent as classes might change while current lookup is being performed
        if (classes.containsKey(name)) {
            return classes.get(name);
        } else {
            ClassType ct = findClassType0(name);
            classes.put(name, Objects.requireNonNull(ct));
            return ct;
        }
    }
}
