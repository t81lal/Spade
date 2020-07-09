package com.krakenrs.spade.pipeline.execution;

import java.util.function.Function;

import com.krakenrs.spade.pipeline.Pipeline;

public class PipelineExecutorImpl<S, O> implements PipelineExecutor<S, O> {
    private Pipeline<S, ?, O> pipeline;
    private Function<S, PipelineExecutionContext<S>> contextCreator;

    public PipelineExecutorImpl(Pipeline<S, ?, O> pipeline) {
        this.pipeline = pipeline;
    }

    public PipelineExecutorImpl(Pipeline<S, ?, O> pipeline, Function<S, PipelineExecutionContext<S>> contextCreator) {
        this.pipeline = pipeline;
        this.contextCreator = contextCreator;
    }

    protected PipelineExecutionContext<S> createContext(S input) {
        if (contextCreator != null) {
            return contextCreator.apply(input);
        } else {
            return new SimplePipelineExecutionContext<>(input);
        }
    }

    @Override
    public O execute(S input) throws ExecutionException {
        PipelineExecutionContext<S> context = createContext(input);
        O result;
        try {
            context.onExecutionStart();
            result = pipeline.get(context);
        } finally {
            context.onExecutionEnd();
        }
        return result;
    }
}
