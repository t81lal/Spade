package com.krakenrs.spade.ir.type;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krakenrs.spade.commons.collections.LazyCreationHashMap;

import lombok.NonNull;

public class TypeTree {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeTree.class);

    private final TypeManager typeManager;
    private final Map<ClassType, Set<ClassType>> subclasses;
    private final Map<ClassType, Set<ClassType>> subInterfaces;
    private final Map<ClassType, Set<ClassType>> implementors;

    public TypeTree(TypeManager typeManager) {
        this.typeManager = typeManager;
        subclasses = new LazyCreationHashMap<>(HashSet::new);
        subInterfaces = new LazyCreationHashMap<>(HashSet::new);
        implementors = new LazyCreationHashMap<>(HashSet::new);

    }

    private boolean isOCSType(ValueType type) {
        return type.equals(typeManager.asObjectType(Object.class))
                || type.equals(typeManager.asObjectType(Serializable.class))
                || type.equals(typeManager.asObjectType(Cloneable.class));
    }

    public boolean canStoreType(@NonNull ValueType child, @NonNull ValueType parent) {
        if (child.equals(parent)) {
            return true;
        } else if (parent.equals(PrimitiveType.NULL)) {
            return false;
        } else if (child.equals(PrimitiveType.NULL)) {
            return parent instanceof ObjectType || parent instanceof ArrayType;
        } else if (child instanceof ObjectType) {
            if (parent instanceof ObjectType) {
                return canStoreClass(((ObjectType) child).getClassType(), ((ObjectType) parent).getClassType());
            } else {
                return false;
            }
        } else if (child instanceof ArrayType) {
            if (parent instanceof ObjectType) {
                return isOCSType(parent);
            } else if (parent instanceof ArrayType) {
                ArrayType arrayP = (ArrayType) parent, arrayC = (ArrayType) child;
                int dimsP = arrayP.dimensions(), dimsC = arrayC.dimensions();
                ValueType baseP = arrayP.componentType(), baseC = arrayC.componentType();

                if (dimsP == dimsC) {
                    // X[] with Y[]
                    if (baseP instanceof ObjectType && baseC instanceof ObjectType) {
                        return canStoreType(baseC, baseP);
                    } else {
                        return false;
                    }
                } else if (dimsC > dimsP) {
                    // int[][][] as Object/Serializable/Cloneable[]
                    return isOCSType(baseP);
                } else {
                    // int[] with X[][]
                    return false;
                }
            } else {
                // Array vs !(array || object)
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean canStoreClass(ClassType child, ClassType parent) {
        ClassType cur = child;
        boolean shouldCheckInterfaces = parent.isInterface();

        while (cur != null) {
            if (cur.equals(parent)) {
                return true;
            }

            if (shouldCheckInterfaces) {
                for (ClassType superInterface : cur.getSuperInterfaceTypes()) {
                    if (canStoreClass(superInterface, parent)) {
                        return true;
                    }
                }
            }

            cur = cur.getSuperClassType();
        }

        return false;
    }

    private boolean checkResolvedClass(ClassType clazz, Object source) {
        if (clazz instanceof UnresolvedClassType) {
            LOGGER.error("Couldn't add {} to type tree from {}", clazz, source);
            return false;
        } else {
            return true;
        }
    }

    public void addClass(@NonNull ClassType clazz) {
        if (!checkResolvedClass(clazz, "DEFAULT"))
            return;

        if (!clazz.isInterface()) {
            ClassType superClazz = clazz.getSuperClassType();
            if (superClazz != null && checkResolvedClass(superClazz, clazz)) {
                subclasses.get(superClazz).add(clazz);
            }
        }

        for (ClassType superInterface : clazz.getSuperInterfaceTypes()) {
            if (!checkResolvedClass(superInterface, clazz))
                continue;

            if (superInterface.isInterface()) {
                subInterfaces.get(superInterface).add(clazz);
            } else {
                implementors.get(superInterface).add(clazz);
            }
        }
    }
}
