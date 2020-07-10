package com.krakenrs.spade.guice;

import com.google.inject.Injector;
import com.krakenrs.spade.pipeline.execution.GuicedPipelineExecutionContext;

import lombok.Getter;
import lombok.NonNull;

public class ContextScopedGuicedPipelineExecutionContext<V> extends GuicedPipelineExecutionContext<V> {
    @Getter @NonNull
    private final ContextScope<V> scope;
    private final Class<V> contextClass;
    
    public ContextScopedGuicedPipelineExecutionContext(V input, Injector injector, ContextScope<V> scope, Class<V> contextClass) {
        super(input, injector);
        this.scope = scope;
        this.contextClass = contextClass;
    }

    @Override
    public void onExecutionStart() {
        V context = getInput();
        scope.enter(context);
        scope.seed(contextClass, context);
    }

    @Override
    public void onExecutionEnd() {
        scope.exit(getInput());
    }
}
