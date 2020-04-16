package com.krakenrs.spade.ir.gen;

import com.krakenrs.spade.ir.code.ControlFlowGraph;
import com.krakenrs.spade.ir.type.ClassType;
import com.krakenrs.spade.ir.type.MethodType;
import com.krakenrs.spade.ir.type.TypeManager;

public class GenerationCtx {

    private final TypeManager typeManager;
    private final ClassType ownerType;
    private final MethodType methodType;
    private final boolean isStaticMethod;

    private final ControlFlowGraph graph;

    public GenerationCtx(TypeManager typeManager, ClassType ownerType, MethodType methodType, boolean isStaticMethod) {
        this.typeManager = typeManager;
        this.ownerType = ownerType;
        this.methodType = methodType;
        this.isStaticMethod = isStaticMethod;

        this.graph = new ControlFlowGraph(methodType, isStaticMethod);
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
