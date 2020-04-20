package com.krakenrs.spade.ir.gen;

import java.lang.reflect.Modifier;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krakenrs.spade.ir.type.TypeManager;

public class AsmGenerationCtx extends GenerationCtx {

    private final MethodNode method;

    public AsmGenerationCtx(TypeManager typeManager, ClassNode klass, MethodNode method) {
        super(createLogger(klass.name, method.name), typeManager, typeManager.asClassType(klass.name),
                typeManager.asMethodType(method.desc),
                Modifier.isStatic(method.access));
        this.method = method;
    }
    
    private static Logger createLogger(String klass, String method) {
        return LoggerFactory.getLogger("AsmGenerator/" + getSimpleName(klass) + "." + method);
    }

    private static String getSimpleName(String className) {
        int lastSlash = className.lastIndexOf('/');
        if (lastSlash == -1) {
            return className;
        } else {
            return className.substring(lastSlash + 1);
        }
    }

    public MethodNode getMethod() {
        return method;
    }
}
