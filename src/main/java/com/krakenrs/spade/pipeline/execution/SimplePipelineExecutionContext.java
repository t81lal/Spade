package com.krakenrs.spade.pipeline.execution;

import java.lang.reflect.InvocationTargetException;

import com.krakenrs.spade.pipeline.PipelineStep;

public class SimplePipelineExecutionContext<V> extends PipelineExecutionContext<V> {
    public SimplePipelineExecutionContext(V input) {
        super(input);
    }

    public <I, O> PipelineStep<I, O> getStep(Class<? extends PipelineStep<I, O>> stepClass) throws ExecutionException {
        try {
            return stepClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new ExecutionException(e);
        }
    }
}
