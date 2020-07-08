package com.krakenrs.spade.pipeline;

import com.krakenrs.spade.pipeline.execution.ExecutionException;
import com.krakenrs.spade.pipeline.execution.PipelineExecutionContext;

public class DeferredPipeline<S, I, O> extends Pipeline<S, I, O> {
    private final Class<? extends PipelineStep<I, O>> stepClass;

    public DeferredPipeline(PipelineHeadStub<S> head, PipelineProducer<S, I> inputProducer,
            Class<? extends PipelineStep<I, O>> stepClass) {
        super(head, inputProducer);
        this.stepClass = stepClass;
    }

    @Override
    public O get(PipelineExecutionContext<S> context) throws ExecutionException {
        PipelineStep<I, O> step = context.getStep(stepClass);
        return step.apply(inputProducer.get(context));
    }
}