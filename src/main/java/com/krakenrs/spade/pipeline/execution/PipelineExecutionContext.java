package com.krakenrs.spade.pipeline.execution;

import com.krakenrs.spade.pipeline.PipelineProducer;
import com.krakenrs.spade.pipeline.PipelineStep;

public abstract class PipelineExecutionContext<V> implements PipelineProducer<V, V> {
    private final V input;

    public PipelineExecutionContext(V input) {
        this.input = input;
    }
    
    public void onExecutionStart() {
    }

    public void onExecutionEnd() {
    }

    public V getInput() {
        return input;
    }

    public abstract <I, O> PipelineStep<I, O> getStep(Class<? extends PipelineStep<I, O>> stepClass)
            throws ExecutionException;

    public abstract <I, O> PipelineStep<I, O> getEmbellishedStep(PipelineStep<I, O> step) throws ExecutionException;

    @Override
    public V get(PipelineExecutionContext<V> ctx) {
        if (ctx != this) {
            throw new IllegalStateException();
        }
        return input;
    }
}
