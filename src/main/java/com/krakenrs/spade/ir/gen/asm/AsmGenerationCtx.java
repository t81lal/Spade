package com.krakenrs.spade.ir.gen.asm;

import java.lang.reflect.Modifier;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krakenrs.spade.ir.gen.GenerationCtx;
import com.krakenrs.spade.ir.type.TypeManager;

import lombok.Getter;
import lombok.NonNull;

public class AsmGenerationCtx extends GenerationCtx {

    @Getter
    private final ClassNode klass;
    @Getter
    private final MethodNode method;

    public AsmGenerationCtx(@NonNull TypeManager typeManager, @NonNull ClassNode klass, @NonNull MethodNode method) {
        super(createLoggerForPhase("AsmGenerator", klass.name, method.name), typeManager.asClassType(klass.name),
                typeManager.asMethodType(method.desc), Modifier.isStatic(method.access));
        this.klass = klass;
        this.method = method;
    }

    @Override
    public void executePhase(String phase, Runnable r) {
        Logger oldLogger = getLogger();
        try {
            Logger newLogger = createLoggerForPhase(phase, klass.name, method.name);
            setLogger(newLogger);
            r.run();
        } finally {
            setLogger(oldLogger);
        }
    }

    private static Logger createLoggerForPhase(String phase, String klass, String method) {
        return LoggerFactory.getLogger(String.format("%s/%s.%s", phase, getSimpleName(klass), method));
    }

    private static String getSimpleName(String className) {
        int lastSlash = className.lastIndexOf('/');
        if (lastSlash == -1) {
            return className;
        } else {
            return className.substring(lastSlash + 1);
        }
    }
}
