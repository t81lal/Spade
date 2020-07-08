package com.krakenrs.spade.pipeline.execution;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.krakenrs.spade.pipeline.PipelineStep;

public class GuicedPipelineExecutionContext<V> extends PipelineExecutionContext<V> {
    private final Injector injector;

    public GuicedPipelineExecutionContext(V input, Injector injector) {
        super(input);
        this.injector = injector;
    }

    public <I, O> PipelineStep<I, O> getStep(Class<? extends PipelineStep<I, O>> stepClass) throws ExecutionException {
        try {
            return injector.getInstance(stepClass);
        } catch (ConfigurationException | ProvisionException e) {
            throw new ExecutionException(e);
        }
    }
}
