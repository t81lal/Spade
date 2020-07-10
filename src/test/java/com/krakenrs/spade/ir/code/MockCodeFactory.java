package com.krakenrs.spade.ir.code;

import com.krakenrs.spade.ir.code.observer.CodeObservationManager;
import com.krakenrs.spade.ir.code.observer.DefaultCodeObservationManager;
import com.krakenrs.spade.ir.type.MethodType;

public class MockCodeFactory implements ControlFlowGraph.Factory, CodeBlock.Factory {
    private static final CodeObservationManager mockCOM = new DefaultCodeObservationManager();

    @Override
    public CodeBlock create(int id) {
        return new CodeBlock(mockCOM, id);
    }

    @Override
    public ControlFlowGraph create(MethodType methodType, boolean isStatic) {
        return new ControlFlowGraph(this, methodType, isStatic);
    }
    
    public static CodeBlock makeBlock(int id) {
        return new MockCodeFactory().create(id);
    }
}
