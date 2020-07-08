package com.krakenrs.spade.pipeline.execution;

import com.krakenrs.spade.pipeline.Pipeline;
import com.krakenrs.spade.pipeline.PipelineStep;

/**
 * A {@link PipelineExecutor} is a component in the pipeline framework that takes a bound {@link Pipeline} and a given
 * input and executes the steps that make up the pipeline to produce a final result.
 * 
 * @see PipelineExecutorImpl
 * 
 * @author Bilal Tariq
 *
 * @param <S> The input type to the entire {@link Pipeline}
 * @param <O> The output type of the entire {@link Pipeline}
 */
public interface PipelineExecutor<S, O> {

    /**
     * Triggers the actual execution of the pipeline with the given input value. All context information is
     * formed by the current executor so that only the input value itself is required here to complete the execution.
     * 
     * @param input The input value to feed into the pipeline
     * @return The result computed by the pipeline
     * @throws ExecutionException If any expectable error occurs, i.e. an error that may happen in the course of an
     *     {@link PipelineStep}'s execution or other configuration problem but <b>not</b> because of a misuse of the
     *     pipeline framework
     */
    O execute(S input) throws ExecutionException;
}
