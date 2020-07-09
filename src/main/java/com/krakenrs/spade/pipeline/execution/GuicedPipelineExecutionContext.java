package com.krakenrs.spade.pipeline.execution;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.krakenrs.spade.pipeline.PipelineStep;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class GuicedPipelineExecutionContext<V> extends PipelineExecutionContext<V> {
    private final Injector injector;
    private final boolean withObjectInjection;

    public GuicedPipelineExecutionContext(V input, Injector injector) {
        this(input, injector, false);
    }

    public GuicedPipelineExecutionContext(V input, Injector injector, boolean withObjectInjection) {
        super(input);
        this.injector = injector;
        this.withObjectInjection = withObjectInjection;
    }

    public <I, O> PipelineStep<I, O> getStep(@NonNull Class<? extends PipelineStep<I, O>> stepClass)
            throws ExecutionException {
        try {
            PipelineStep<I, O> actualStep = injector.getInstance(stepClass);
            return new GuicedPipelineStep<>(actualStep);
        } catch (ConfigurationException | ProvisionException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public <I, O> PipelineStep<I, O> getEmbellishedStep(PipelineStep<I, O> step) throws ExecutionException {
        if (!withObjectInjection) {
            return step;
        }

        if (step instanceof GuicedPipelineStep) {
            return step;
        } else {
            injector.injectMembers(step);
            return new GuicedPipelineStep<>(step);
        }
    }

    @RequiredArgsConstructor
    static class GuicedPipelineStep<I, O> implements PipelineStep<I, O> {
        @NonNull
        PipelineStep<I, O> actualStep;

        @Override
        public O apply(I t) {
            return actualStep.apply(t);
        }
    }
}
