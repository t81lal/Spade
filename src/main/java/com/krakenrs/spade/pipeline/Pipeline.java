package com.krakenrs.spade.pipeline;

import java.util.function.Function;

import com.krakenrs.spade.pipeline.execution.PipelineExecutionContext;
import com.krakenrs.spade.pipeline.execution.PipelineExecutor;
import com.krakenrs.spade.pipeline.execution.PipelineExecutorImpl;

/**
 * Abstraction for a pipeline. A pipeline is a sequence of computations that use the result of the previous computation
 * and pass along their output to the next.
 * This is very similar to function composition, except different execution strategies can be used to control the
 * context in which the computations are performed. This is useful as values can be injected and retrieved as if they
 * were managed by a heavy container, but instead they are only very lightly encapsulated.
 * <p>
 * This class acts as a base class for other more specific pipeline steps such as {@link ApplicativePipeline} and
 * {@link DeferredPipeline} and is not something a client should try to use. Instead it acts as an interface to the
 * fluent API that is used to build and execute pipelines.
 * <p>
 * Clients should begin a pipeline by calling the {@link #from()} method and applying computations sequentially. Then
 * the pipeline must be compiled using the {@link #build()} methods, resulting in a {@link PipelineExecutor} which can
 * be directly invoked by code that requires execution of the pipeline.
 * 
 * @author Bilal Tariq
 *
 * @param <S> The input type of the entire pipeline
 * @param <I> The input type to this particular part of the pipeline
 * @param <O> The output type of this particular part of the pipeline
 */
public abstract class Pipeline<S, I, O>
        implements PipelineProducer<S, O>, PipelineComposable<S, O>, PipelineComposed<S, I, O> {
    protected final PipelineHeadStub<S> head;
    protected final PipelineProducer<S, I> inputProducer;

    public Pipeline(PipelineHeadStub<S> head, PipelineProducer<S, I> inputProducer) {
        this.head = head;
        this.inputProducer = inputProducer;
    }

    @Override
    public <T> Pipeline<S, O, T> then(PipelineStep<O, T> step) {
        return new ApplicativePipeline<>(head, this, step);
    }

    @Override
    public <T> Pipeline<S, O, T> then(Class<? extends PipelineStep<O, T>> stepClass) {
        return new DeferredPipeline<>(head, this, stepClass);
    }

    /**
     * Creates a default {@link PipelineExecutor} that acts as if each computation is composed sequentially (like
     * function composition).
     * 
     * @return A basic executor
     */
    @Override
    public PipelineExecutor<S, O> build() {
        return new PipelineExecutorImpl<>(this);
    }

    /**
     * Creates a {@link PipelineExecutor} from the given subclass for this pipeline. The given {@link PipelineExecutor}
     * must have a constructor that takes a single {@link Pipeline} object as it's argument.
     * 
     * @param executorClass The executor class to instantiate
     * @return An executor from the given type
     * @throws Exception If any reflection errors occur when instantiating the executor
     */
    @Override
    public PipelineExecutor<S, O> build(Class<? extends PipelineExecutor<S, O>> executorClass) throws Exception {
        return executorClass.getConstructor(Pipeline.class).newInstance(this);
    }

    /**
     * Creates a {@link PipelineExecutor} using a custom {@link PipelineExecutionContext} factory. This is useful when
     * the context needs to be dynamically created or configured.
     * 
     * @param contextCreator The {@link PipelineExecutionContext} factory
     * @return An executor that uses the given context factory
     */
    @Override
    public PipelineExecutor<S, O> build(Function<S, PipelineExecutionContext<S>> contextCreator) {
        return new PipelineExecutorImpl<>(this, contextCreator);
    }

    /**
     * Fluent API method that facilitates creating the beginning of a pipeline.
     * 
     * @param <T>
     * @return
     */
    public static <T> PipelineComposable<T, T> from() {
        return new PipelineHeadStub<>();
    }
}
