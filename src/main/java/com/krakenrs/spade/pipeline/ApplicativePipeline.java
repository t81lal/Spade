package com.krakenrs.spade.pipeline;

import com.krakenrs.spade.pipeline.execution.ExecutionException;
import com.krakenrs.spade.pipeline.execution.PipelineExecutionContext;

public class ApplicativePipeline<S, I, O> extends Pipeline<S, I, O> {
    private final PipelineStep<I, O> step;

    public ApplicativePipeline(PipelineHeadStub<S> head, PipelineProducer<S, I> inputProducer,
            PipelineStep<I, O> step) {
        super(head, inputProducer);
        this.step = step;
    }

    @Override
    public O get(PipelineExecutionContext<S> context) throws ExecutionException {
        PipelineStep<I, O> embellishedStep = context.getEmbellishedStep(step);
        return embellishedStep.apply(inputProducer.get(context));
    }
}
