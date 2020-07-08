package com.krakenrs.spade.pipeline;

import java.util.function.Function;

import com.krakenrs.spade.pipeline.execution.PipelineExecutionContext;
import com.krakenrs.spade.pipeline.execution.PipelineExecutor;

/**
 * Represents a pipeline component that <b>has been</b> and can be composed.
 * 
 * @see PipelineComposable
 * 
 * @author Bilal Tariq
 *
 * @param <S> The input type of the entire pipeline
 * @param <I> The input type to this particular part of the pipeline
 */
public interface PipelineComposed<S, I, O> extends PipelineComposable<S, O> {

    /**
     * Creates a default {@link PipelineExecutor} that acts as if each computation is composed sequentially (like
     * function composition).
     * 
     * @return A basic executor
     */
    PipelineExecutor<S, O> build();

    /**
     * Creates a {@link PipelineExecutor} from the given subclass for this pipeline. The given {@link PipelineExecutor}
     * must have a constructor that takes a single {@link Pipeline} object as it's argument.
     * 
     * @param executorClass The executor class to instantiate
     * @return An executor from the given type
     * @throws Exception If any reflection errors occur when instantiating the executor
     */
    PipelineExecutor<S, O> build(Class<? extends PipelineExecutor<S, O>> executorClass) throws Exception;

    /**
     * Creates a {@link PipelineExecutor} using a custom {@link PipelineExecutionContext} factory. This is useful when
     * the context needs to be dynamically created or configured.
     * 
     * @param contextCreator The {@link PipelineExecutionContext} factory
     * @return An executor that uses the given context factory
     */
    PipelineExecutor<S, O> build(Function<S, PipelineExecutionContext<S>> contextCreator);
}
