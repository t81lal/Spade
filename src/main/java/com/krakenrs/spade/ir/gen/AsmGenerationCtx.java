package com.krakenrs.spade.ir.gen;

import java.lang.reflect.Modifier;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.krakenrs.spade.ir.type.TypeManager;

public class AsmGenerationCtx extends GenerationCtx {

    private final MethodNode method;

    public AsmGenerationCtx(TypeManager typeManager, ClassNode klass, MethodNode method) {
        super(typeManager, typeManager.asClassType(klass.name), typeManager.asMethodType(method.desc),
                Modifier.isStatic(method.access));
        this.method = method;
    }
    
    public MethodNode getMethod() {
        return method;
    }
}
