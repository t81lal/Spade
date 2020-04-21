package com.krakenrs.spade.ir.gen;

import java.util.function.Supplier;

import org.slf4j.Logger;

import com.krakenrs.spade.ir.code.ControlFlowGraph;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.MethodType;
import com.krakenrs.spade.ir.type.TypeManager;

public class GenerationCtx {

    private final Logger logger;
    private final TypeManager typeManager;
    private final ClassType ownerType;
    private final MethodType methodType;
    private final boolean isStaticMethod;

    private final ControlFlowGraph graph;

    public GenerationCtx(Logger logger, TypeManager typeManager, ClassType ownerType, MethodType methodType,
            boolean isStaticMethod) {
        this.logger = logger;
        this.typeManager = typeManager;
        this.ownerType = ownerType;
        this.methodType = methodType;
        this.isStaticMethod = isStaticMethod;

        this.graph = new ControlFlowGraph(methodType, isStaticMethod);
    }

    public void executeStage(String name, Runnable r) {
        long startTime = System.nanoTime();
        logger.trace("Executing {}", name);
        r.run();
        long elapsedTime = System.nanoTime() - startTime;
        double elapsedMs = elapsedTime / 1000000D;
        logger.trace(" took {}ms", elapsedMs);
    }

    public <T> T executeStage(String name, Supplier<T> r) {
        long startTime = System.nanoTime();
        logger.trace("Executing {}", name);
        T res = r.get();
        long elapsedTime = System.nanoTime() - startTime;
        double elapsedMs = elapsedTime / 1000000D;
        logger.trace(" took {}ms", elapsedMs);
        return res;
    }

    public Logger getLogger() {
        return logger;
    }

    public TypeManager getTypeManager() {
        return typeManager;
    }

    public ClassType getOwnerType() {
        return ownerType;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public boolean isStaticMethod() {
        return isStaticMethod;
    }

    public ControlFlowGraph getGraph() {
        return graph;
    }
}
