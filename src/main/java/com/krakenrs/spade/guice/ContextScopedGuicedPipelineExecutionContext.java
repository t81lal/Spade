package com.krakenrs.spade.guice;

import com.google.inject.Injector;
import com.krakenrs.spade.pipeline.execution.GuicedPipelineExecutionContext;

import lombok.Getter;
import lombok.NonNull;

public class ContextScopedGuicedPipelineExecutionContext<V> extends GuicedPipelineExecutionContext<V> {
    @Getter @NonNull
    private final ContextScope<V> scope;

    public ContextScopedGuicedPipelineExecutionContext(V input, Injector injector, ContextScope<V> scope) {
        super(input, injector);
        this.scope = scope;
    }

    @Override
    public void onExecutionStart() {
        scope.enter(getInput());
    }

    @Override
    public void onExecutionEnd() {
        scope.exit(getInput());
    }
}
