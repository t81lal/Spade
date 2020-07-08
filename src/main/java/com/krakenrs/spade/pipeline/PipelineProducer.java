package com.krakenrs.spade.pipeline;

import com.krakenrs.spade.pipeline.execution.ExecutionException;
import com.krakenrs.spade.pipeline.execution.PipelineExecutionContext;

/**
 * Represents a value producer that acts under a context.
 * <p>
 * This class is semi-internal, that is, user code should not care about it and treat it as an intermediary object when
 * using the fluent API. Some of the methods that must be defined are done so to streamline the framework's API.
 * 
 * @author Bilal Tariq
 *
 * @param <S> The type of the input value of the pipeline
 * @param <I> The type of the values this producer produces
 */
public interface PipelineProducer<S, I> {

    /**
     * Possibly performs a computation to produce a value.
     * 
     * @param context A context that may affect the value produced
     * @return The value
     * @throws ExecutionException If any expectable error occurs, i.e. an error that may happen in the course of the
     *     computation that was <b>not</b> caused because of a misuse of the
     *     pipeline framework
     */
    I get(PipelineExecutionContext<S> context) throws ExecutionException;
}
