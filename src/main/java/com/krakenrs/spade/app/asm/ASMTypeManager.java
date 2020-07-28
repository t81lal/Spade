package com.krakenrs.spade.app.asm;

import java.util.Set;
import java.util.stream.Collectors;

import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.ResolvedClassType;
import com.krakenrs.spade.ir.type.TypeManager;
import com.krakenrs.spade.ir.type.UnresolvedClassType;

import lombok.Getter;

public class ASMTypeManager extends TypeManager {
    @Getter
    private final KlassScene scene;

    public ASMTypeManager(KlassScene scene) {
        this.scene = scene;
    }

    @Override
    protected ClassType findClassType(String name) {
        Klass klass = scene.findKlass(name);

        if (klass == null) {
            return new UnresolvedClassType(name);
        }

        ClassType superClass = null;
        if (klass.superName == null) {
            if (!name.equals("java/lang/Object")) {
                throw new IllegalStateException(name + " has no super class");
            }
        } else {
            superClass = asClassType(klass.superName);
        }

        Set<ClassType> superInterfaces = klass.interfaces.stream().map(this::asClassType).collect(Collectors.toSet());
        return new ResolvedClassType(name, superClass, superInterfaces, klass.access);
    }
}
