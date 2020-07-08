package com.krakenrs.spade.pipeline;

import com.krakenrs.spade.pipeline.execution.PipelineExecutionContext;

public class PipelineHeadStub<V> implements PipelineProducer<V, V> {
    @Override
    public <T> Pipeline<V, V, T> then(PipelineStep<V, T> step) {
        return new ApplicativePipeline<>(this, this, step);
    }

    @Override
    public <T> Pipeline<V, V, T> then(Class<? extends PipelineStep<V, T>> stepClass) {
        return new DeferredPipeline<>(this, this, stepClass);
    }

    @Override
    public V get(PipelineExecutionContext<V> context) {
        return context.getInput();
    }
}
